package cl.duoc.backendiii.semana1;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import javax.sql.DataSource;

/**
 * Configuración principal de Spring Batch para la Semana 1.
 *
 * Esta clase es el corazón del ejemplo. Aquí se declara la arquitectura batch
 * completa: de dónde se leen los datos, cómo se transforman, dónde se escriben
 * y cómo Spring Batch organiza todo dentro de un Job y un Step.
 *
 * Mapa conceptual de la Semana 1: 
 *
 * Job: migrarClientesLegacyJob
 * └── Step: transformarClientesStep
 *     ├── ItemReader    -> lee CSV legacy
 *     ├── ItemProcessor -> normaliza/enriquece datos
 *     └── ItemWriter    -> escribe CSV moderno
 *
 * Conceptos importantes:
 * - JobRepository: almacena metadata de ejecución del Job y sus Steps.
 * - PlatformTransactionManager: administra transacciones por chunk.
 * - Chunk: grupo de registros que se lee/procesa/escribe antes de confirmar.
 */
@Configuration
public class BatchConfiguration {

    /**
     * ItemReader: componente encargado de leer datos desde la fuente legacy.
     *
     * En este ejemplo, la fuente legacy es un archivo CSV ubicado en:
     * src/main/resources/input/clientes_legacy.csv
     *
     * FlatFileItemReader lee el archivo línea por línea y transforma cada línea
     * en un objeto Java de tipo LegacyCustomer.
     *
     * Flujo de esta parte:
     * línea CSV -> FieldSet -> LegacyCustomer
     */
    @Bean
    public FlatFileItemReader<LegacyCustomer> reader() {
        return new FlatFileItemReaderBuilder<LegacyCustomer>()
                // Nombre interno del reader. Sirve para trazabilidad dentro de Spring Batch.
                .name("legacyCustomerReader")

                // El archivo está dentro del classpath, en src/main/resources/input.
                .resource(new ClassPathResource("input/clientes_legacy.csv"))

                // Saltamos la primera línea porque es la cabecera del CSV.
                .linesToSkip(1)

                // Indicamos que el archivo es delimitado, no de ancho fijo.
                .delimited()

                // El sistema legacy usa punto y coma como separador.
                .delimiter(";")

                // Nombres lógicos para cada columna. Luego se usan en el mapper.
                .names("idLegacy", "nombre", "apellido", "fechaNacimiento", "deudaPesos", "correo")

                // fieldSetMapper convierte las columnas leídas en un record LegacyCustomer.
                .fieldSetMapper(fieldSet -> new LegacyCustomer(
                        fieldSet.readString("idLegacy"),
                        fieldSet.readString("nombre"),
                        fieldSet.readString("apellido"),
                        fieldSet.readString("fechaNacimiento"),
                        fieldSet.readString("deudaPesos"),
                        fieldSet.readString("correo")
                ))
                .build();
    }

    /**
     * ItemReader para el proceso real del Banco XYZ: Job 1 (Transacciones Diarias).
     *
     * Lee el archivo src/main/resources/input/transacciones.csv y convierte
     * cada línea en un objeto Transaccion.
     *
     * A diferencia del reader de LegacyCustomer (que arma el objeto campo por
     * campo con fieldSet.readString), aquí usamos BeanWrapperFieldSetMapper:
     * como Transaccion tiene setters, Spring puede "adivinar" solo qué setter
     * usar para cada columna, siempre que el nombre coincida exactamente.
     */
    @Bean
    public FlatFileItemReader<Transaccion> transaccionReader() {
        return new FlatFileItemReaderBuilder<Transaccion>()
                .name("transaccionReader")
                .resource(new ClassPathResource("input/transacciones.csv"))
                .linesToSkip(1)
                .delimited()
                .delimiter(",")
                .names("id", "fecha", "monto", "tipo")
                .fieldSetMapper(fieldSet -> {
                    Transaccion t = new Transaccion();
                    t.setId(fieldSet.readLong("id"));
                    t.setFecha(LocalDate.parse(fieldSet.readString("fecha")));
                    t.setMonto(fieldSet.readBigDecimal("monto"));
                    t.setTipo(fieldSet.readString("tipo"));
                    return t;
                })
                .build();
    }

        /**
     * Envuelve transaccionReader en un decorador seguro para múltiples hilos.
     * Necesario porque el Step usa taskExecutor (3 hilos en paralelo) y
     * FlatFileItemReader no es seguro para lecturas concurrentes por sí solo.
     */
    @Bean
    public SynchronizedItemStreamReader<Transaccion> transaccionReaderSincronizado(
            FlatFileItemReader<Transaccion> transaccionReader
    ) {
        SynchronizedItemStreamReader<Transaccion> readerSincronizado = new SynchronizedItemStreamReader<>();
        readerSincronizado.setDelegate(transaccionReader);
        return readerSincronizado;
    }

    

        /**
     * TaskExecutor para el procesamiento paralelo del Job 1.
     *
     * El enunciado pide 3 hilos de ejecución paralela. A diferencia del
     * ejemplo del profe (que usa 5-10 hilos, pensado para más carga),
     * aquí fijamos core=3 y max=3 para cumplir exactamente lo pedido:
     * nunca va a crear más de 3 hilos, ni menos.
     */
    @Bean
    public TaskExecutor batchTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("Transaccion-Thread-");
        executor.setDaemon(true);
        executor.initialize();
        return executor;
    }

        /**
     * ItemWriter para el Job 1 (Transacciones Diarias).
     *
     * Escribe cada TransaccionProcesada en la tabla `transacciones` de MySQL,
     * incluyendo si fue marcada como anomalía y por qué.
     *
     * A diferencia del writer de ModernCustomer (que escribe a un CSV),
     * este usa JdbcBatchItemWriter: ejecuta un INSERT SQL por cada registro,
     * agrupados en lotes (chunks) para mayor eficiencia.
     */
    @Bean
    public JdbcBatchItemWriter<TransaccionProcesada> transaccionWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<TransaccionProcesada>()
                .dataSource(dataSource)
                .sql("INSERT INTO transacciones (id, fecha, monto, tipo, es_anomalia, motivo_anomalia) "
                        + "VALUES (:id, :fecha, :monto, :tipo, :esAnomalia, :motivoAnomalia)")
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .build();
    }

    @Bean
    public TransaccionProcessor transaccionProcessor() {
        return new TransaccionProcessor();
    }

    @Bean
    public TransaccionSkipPolicy transaccionSkipPolicy() {
        return new TransaccionSkipPolicy();
    }

    @Bean
    public TransaccionSkipListener transaccionSkipListener() {
        return new TransaccionSkipListener();
    }

    

    /**
     * ItemProcessor: componente encargado de transformar cada registro.
     *
     * Spring Batch llamará a este processor una vez por cada LegacyCustomer leído.
     * La salida será un ModernCustomer, que representa el formato objetivo.
     */
    @Bean
    public LegacyCustomerProcessor processor() {
        return new LegacyCustomerProcessor();
    }

        /**
     * Step del Job 1: procesa el archivo transacciones.csv completo,
     * detectando anomalías y guardando el resultado en MySQL.
     */
    @Bean
    public Step procesarInteresesStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            SynchronizedItemStreamReader<CuentaInteres> cuentaInteresReaderSincronizado,
            CuentaInteresProcessor cuentaInteresProcessor,
            JdbcBatchItemWriter<CuentaInteresProcesada> cuentaInteresWriter,
            TaskExecutor batchTaskExecutor,
            CuentaInteresSkipPolicy cuentaInteresSkipPolicy,
            CuentaInteresSkipListener cuentaInteresSkipListener
    ) {
        return new StepBuilder("procesarInteresesStep", jobRepository)
                .<CuentaInteres, CuentaInteresProcesada>chunk(5, transactionManager)
                .reader(cuentaInteresReaderSincronizado)
                .processor(cuentaInteresProcessor)
                .writer(cuentaInteresWriter)
                .faultTolerant()
                .retry(TransientDataAccessException.class)
                .retryLimit(2)
                .skipPolicy(cuentaInteresSkipPolicy)
                .listener(cuentaInteresSkipListener)
                .taskExecutor(batchTaskExecutor)
                .build();
    }

        /**
     * Step del Job 1: procesa el archivo transacciones.csv completo,
     * detectando anomalías y guardando el resultado en MySQL.
     */
    @Bean
    public Step procesarTransaccionesStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            SynchronizedItemStreamReader<Transaccion> transaccionReaderSincronizado,
            TransaccionProcessor transaccionProcessor,
            JdbcBatchItemWriter<TransaccionProcesada> transaccionWriter,
            TaskExecutor batchTaskExecutor,
            TransaccionSkipPolicy transaccionSkipPolicy,
            TransaccionSkipListener transaccionSkipListener
    ) {
        return new StepBuilder("procesarTransaccionesStep", jobRepository)
                .<Transaccion, TransaccionProcesada>chunk(5, transactionManager)
                .reader(transaccionReaderSincronizado)
                .processor(transaccionProcessor)
                .writer(transaccionWriter)
                .faultTolerant()
                .retry(TransientDataAccessException.class)
                .retryLimit(2)
                .skipPolicy(transaccionSkipPolicy)
                .listener(transaccionSkipListener)
                .taskExecutor(batchTaskExecutor)
                .build();
    }

    @Bean
    public Job procesarInteresesJob(
            JobRepository jobRepository,
            Step procesarInteresesStep,
            CuentaInteresProcessor cuentaInteresProcessor
    ) {
        return new JobBuilder("procesarInteresesJob", jobRepository)
                .listener(new CuentaInteresJobCompletionListener(cuentaInteresProcessor))
                .start(procesarInteresesStep)
                .build();
    }

        /**
     * Lanza manualmente el Job 1 cada vez que arranca la aplicación,
     * generando un parámetro único (timestamp) para evitar el problema
     * conocido de Spring Boot donde el incrementer no siempre se activa
     * si ya existe una ejecución previa con parámetros vacíos.
     */
    @Bean
    public CommandLineRunner ejecutarProcesarTransaccionesJob(
            JobLauncher jobLauncher,
            Job procesarTransaccionesJob
    ) {
        return args -> {
            JobParameters parametros = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(procesarTransaccionesJob, parametros);
        };
    }

    /**
     * Job 1: Reporte de Transacciones Diarias.
     * Detecta anomalías en las transacciones y las guarda en MySQL,
     * marcadas según corresponda.
     */
    @Bean
    public Job procesarTransaccionesJob(
            JobRepository jobRepository,
            Step procesarTransaccionesStep,
            TransaccionProcessor transaccionesProcessor

    ) {
        return new JobBuilder("procesarTransaccionesJob", jobRepository)
        .incrementer(new RunIdIncrementer())
                .listener(new TransaccionJobCompletionListener(transaccionesProcessor))
                .start(procesarTransaccionesStep)
                .build();
    }

    /**
     * ItemWriter: componente encargado de escribir los datos transformados.
     *
     * En este ejemplo, escribe un archivo CSV moderno en:
     * output/clientes_modernos.csv
     *
     * A diferencia del reader, este archivo no vive dentro de resources. Se genera
     * en la carpeta output para que pueda revisarse después de ejecutar la demo.
     */
    @Bean
    public FlatFileItemWriter<ModernCustomer> writer() {
        return new FlatFileItemWriterBuilder<ModernCustomer>()
                // Nombre interno del writer.
                .name("modernCustomerWriter")

                // Destino físico del archivo generado.
                .resource(new FileSystemResource("output/clientes_modernos.csv"))

                // Cabecera del CSV moderno.
                .headerCallback(writer -> writer.write("customer_id;full_name;birth_date;debt_amount;email;risk_segment"))

                // Convierte cada ModernCustomer en una línea de texto separada por punto y coma.
                .lineAggregator(customer -> String.join(";",
                        customer.customerId(),
                        customer.fullName(),
                        customer.birthDate().toString(),
                        customer.debtAmount().toPlainString(),
                        customer.email(),
                        customer.riskSegment()
                ))
                .build();
    }

    /**
     * Step: etapa concreta del proceso batch.
     *
     * Este Step conecta los tres componentes principales:
     * - reader: lee LegacyCustomer
     * - processor: transforma LegacyCustomer en ModernCustomer
     * - writer: escribe ModernCustomer en el archivo final
     *
     * La línea .<LegacyCustomer, ModernCustomer>chunk(2, transactionManager)
     * significa:
     * - Tipo de entrada del Step: LegacyCustomer
     * - Tipo de salida del Step: ModernCustomer
     * - Tamaño del chunk: 2 registros
     *
     * Con chunk(2), Spring Batch hace algo parecido a:
     * 1. leer 2 registros,
     * 2. procesar 2 registros,
     * 3. escribir 2 registros,
     * 4. confirmar la transacción.
     */
    @Bean
    public Step transformarClientesStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<LegacyCustomer> reader,
            LegacyCustomerProcessor processor,
            FlatFileItemWriter<ModernCustomer> writer
    ) {
        return new StepBuilder("transformarClientesStep", jobRepository)
                .<LegacyCustomer, ModernCustomer>chunk(2, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    /**
     * Job: proceso batch completo.
     *
     * En Semana 1 basta con un Job de un solo Step. En escenarios reales, un Job
     * puede tener varios Steps: validar archivo, transformar datos, guardar en BD,
     * generar resumen, notificar, etc.
     *
     * Este Job usa un listener para mostrar en consola si terminó correctamente.
     */
    @Bean
    public Job migrarClientesLegacyJob(
            JobRepository jobRepository,
            Step transformarClientesStep
    ) {
        return new JobBuilder("migrarClientesLegacyJob", jobRepository)
                .listener(new JobCompletionNotificationListener())
                .start(transformarClientesStep)
                .build();
    }

    @Bean
    public FlatFileItemReader<CuentaInteres> cuentaInteresReader() {
        return new FlatFileItemReaderBuilder<CuentaInteres>()
                .name("cuentaInteresReader")
                .resource(new ClassPathResource("input/intereses.csv"))
                .linesToSkip(1)
                .delimited()
                .delimiter(",")
                .names("cuenta_id", "nombre", "saldo", "edad", "tipo")
                .fieldSetMapper(fieldSet -> {
                    CuentaInteres c = new CuentaInteres();
                    c.setCuentaId(fieldSet.readLong("cuenta_id"));
                    c.setNombre(fieldSet.readString("nombre").trim());

                    String saldoTexto = fieldSet.readString("saldo").trim();
                    c.setSaldo(saldoTexto.isEmpty() ? null : new BigDecimal(saldoTexto));

                    String edadTexto = fieldSet.readString("edad").trim();
                    c.setEdad(edadTexto.isEmpty() ? null : Integer.parseInt(edadTexto));

                    c.setTipo(fieldSet.readString("tipo").trim());
                    return c;
                })
                .build();
    }

    @Bean
    public SynchronizedItemStreamReader<CuentaInteres> cuentaInteresReaderSincronizado(
            FlatFileItemReader<CuentaInteres> cuentaInteresReader
    ) {
        SynchronizedItemStreamReader<CuentaInteres> readerSincronizado = new SynchronizedItemStreamReader<>();
        readerSincronizado.setDelegate(cuentaInteresReader);
        return readerSincronizado;
    }

    @Bean
    public JdbcBatchItemWriter<CuentaInteresProcesada> cuentaInteresWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<CuentaInteresProcesada>()
                .dataSource(dataSource)
                .sql("INSERT INTO cuentas_intereses (cuenta_id, nombre, saldo_original, saldo_final, edad, tipo, es_anomalia, motivo_anomalia) "
                        + "VALUES (:cuentaId, :nombre, :saldoOriginal, :saldoFinal, :edad, :tipo, :esAnomalia, :motivoAnomalia)")
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .build();
    }

    @Bean
    public CuentaInteresProcessor cuentaInteresProcessor() {
        return new CuentaInteresProcessor();
    }

    @Bean
    public CuentaInteresSkipPolicy cuentaInteresSkipPolicy() {
        return new CuentaInteresSkipPolicy();
    }

    @Bean
    public CuentaInteresSkipListener cuentaInteresSkipListener() {
        return new CuentaInteresSkipListener();
    }

    @Bean
    public CommandLineRunner ejecutarProcesarInteresesJob(
            JobLauncher jobLauncher,
            Job procesarInteresesJob
    ) {
        return args -> {
            JobParameters parametros = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(procesarInteresesJob, parametros);
        };
    }

    /**
   * Reader particionado para el Job 3.
   *
   * @StepScope es la clave: Spring crea UNA instancia de este reader
   * por cada partición, no una sola compartida. Los valores start/end
   * vienen del ExecutionContext que EstadoCuentaAnualPartitioner generó.
   */
    @Bean
    @StepScope
    public FlatFileItemReader<TransaccionAnual> transaccionAnualReader(
        @Value("#{stepExecutionContext['start']}") Integer start,
        @Value("#{stepExecutionContext['end']}") Integer end
    ) {
        int inicioSeguro = (start == null) ? 0 : start;
        int finSeguro = (end == null) ? 0 : end;

        return new FlatFileItemReaderBuilder<TransaccionAnual>()
            .name("transaccionAnualReader-" + inicioSeguro + "-" + finSeguro)
            .resource(new ClassPathResource("input/cuentas_anuales.csv"))
            .linesToSkip(1)
            .currentItemCount(inicioSeguro)
            .maxItemCount(finSeguro + 1)
            .delimited()
            .delimiter(",")
            .names("cuenta_id", "fecha", "transaccion", "monto", "descripcion")
            .fieldSetMapper(fieldSet -> {
                TransaccionAnual t = new TransaccionAnual();
                t.setCuentaId(fieldSet.readLong("cuenta_id"));
                t.setFecha(fieldSet.readString("fecha"));
                t.setTransaccion(fieldSet.readString("transaccion"));
                t.setMonto(fieldSet.readString("monto"));
                t.setDescripcion(fieldSet.readString("descripcion"));
                return t;
            })
            .build();
   }

    @Bean
    public JdbcBatchItemWriter<TransaccionAnualProcesada> transaccionAnualWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<TransaccionAnualProcesada>()
            .dataSource(dataSource)
            .sql("INSERT INTO transacciones_anuales_detalle "
                    + "(cuenta_id, fecha, transaccion, monto, descripcion, es_anomalia, motivo_anomalia) "
                    + "VALUES (:cuentaId, :fecha, :transaccion, :monto, :descripcion, :esAnomalia, :motivoAnomalia)")
            .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
            .build();
   }

    @Bean
    public TransaccionAnualProcessor transaccionAnualProcessor() {
        return new TransaccionAnualProcessor();  
  }

   @Bean
   public TaskExecutor estadoAnualTaskExecutor(
        @Value("${app.estado-anual.grid-size}") int gridSize
  ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(gridSize);
        executor.setMaxPoolSize(gridSize);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("EstadoAnual-Thread-");
        executor.setDaemon(true);
        executor.initialize();
        return executor;
   }

    @Bean
    public Step procesarTransaccionAnualStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        FlatFileItemReader<TransaccionAnual> transaccionAnualReader,
        TransaccionAnualProcessor transaccionAnualProcessor,
        JdbcBatchItemWriter<TransaccionAnualProcesada> transaccionAnualWriter
   ) { 
        return new StepBuilder("procesarTransaccionAnualStep", jobRepository)
            .<TransaccionAnual, TransaccionAnualProcesada>chunk(20, transactionManager)
            .reader(transaccionAnualReader)
            .processor(transaccionAnualProcessor)
            .writer(transaccionAnualWriter)
            .faultTolerant()
            .retry(TransientDataAccessException.class)
            .retryLimit(2)
            .build();
   }

    @Bean
    public TaskExecutorPartitionHandler estadoAnualPartitionHandler(
        Step procesarTransaccionAnualStep,
        TaskExecutor estadoAnualTaskExecutor,
        @Value("${app.estado-anual.grid-size}") int gridSize
   ) {
        TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
        handler.setStep(procesarTransaccionAnualStep);
        handler.setTaskExecutor(estadoAnualTaskExecutor);
        handler.setGridSize(gridSize);
        return handler;
    }

     @Bean
     public Step particionarTransaccionAnualStep(
        JobRepository jobRepository,
        EstadoCuentaAnualPartitioner estadoCuentaAnualPartitioner,
        TaskExecutorPartitionHandler estadoAnualPartitionHandler
   ) {
        return new StepBuilder("particionarTransaccionAnualStep", jobRepository)
            .partitioner("procesarTransaccionAnualStep", estadoCuentaAnualPartitioner)
            .partitionHandler(estadoAnualPartitionHandler)
            .build();
   }

   @Bean
   public Step generarEstadoCuentaAnualStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        DataSource dataSource
   ) {
        return new StepBuilder("generarEstadoCuentaAnualStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {

                JdbcTemplate jdbc = new JdbcTemplate(dataSource);

                // Limpia el resumen anterior antes de recalcular
                jdbc.update("DELETE FROM estados_cuenta_anuales");

                jdbc.update("""
                        INSERT INTO estados_cuenta_anuales
                            (cuenta_id, total_depositos, total_retiros_compras_pagos,
                             saldo_neto_anual, cantidad_transacciones, cantidad_anomalias)
                        SELECT
                            cuenta_id,
                            COALESCE(SUM(CASE WHEN transaccion = 'deposito' AND es_anomalia = false
                                          THEN monto ELSE 0 END), 0) AS total_depositos,
                            COALESCE(SUM(CASE WHEN transaccion IN ('retiro','compra','pago') AND es_anomalia = false
                                          THEN monto ELSE 0 END), 0) AS total_retiros_compras_pagos,
                            COALESCE(SUM(CASE WHEN es_anomalia = false AND transaccion = 'deposito'
                                          THEN monto
                                          WHEN es_anomalia = false
                                          THEN -monto ELSE 0 END), 0) AS saldo_neto_anual,
                            COUNT(*) AS cantidad_transacciones,
                            SUM(CASE WHEN es_anomalia = true THEN 1 ELSE 0 END) AS cantidad_anomalias
                        FROM transacciones_anuales_detalle
                        GROUP BY cuenta_id
                        """);

                System.out.println("Estado de cuenta anual generado correctamente por cuenta.");

                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
   }

   @Bean
   public Job procesarEstadoCuentaAnualJob(
        JobRepository jobRepository,
        Step particionarTransaccionAnualStep,
        Step generarEstadoCuentaAnualStep,
        TransaccionAnualProcessor transaccionAnualProcessor
   ) {
        return new JobBuilder("procesarEstadoCuentaAnualJob", jobRepository)
            .listener(new EstadoCuentaAnualJobCompletionListener(transaccionAnualProcessor))
            .start(particionarTransaccionAnualStep)
            .next(generarEstadoCuentaAnualStep)
            .build();
   }

   @Bean
   public CommandLineRunner ejecutarProcesarEstadoCuentaAnualJob(
        JobLauncher jobLauncher,
        Job procesarEstadoCuentaAnualJob
  ) {
        return args -> {
        JobParameters parametros = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(procesarEstadoCuentaAnualJob, parametros);
    };
  }


}
 