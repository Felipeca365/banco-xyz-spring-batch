package cl.duoc.backendiii.semana1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

/**
 * Listener didáctico para observar el cierre del Job.
 *
 * Un listener en Spring Batch permite ejecutar lógica antes o después de un Job
 * o de un Step. En este ejemplo lo usamos solo para imprimir un mensaje claro
 * en consola cuando la migración termina.
 *
 * Para Semana 1, lo importante es entender que Spring Batch no solo procesa
 * archivos: también conoce el estado de la ejecución mediante JobExecution.
 */
public class JobCompletionNotificationListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

    /**
     * Método llamado automáticamente por Spring Batch cuando el Job termina.
     *
     * jobExecution contiene metadata de la ejecución:
     * - nombre del Job,
     * - estado final,
     * - hora de inicio/fin,
     * - errores,
     * - información de los Steps.
     */
    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            // Mensaje de éxito para que el estudiante sepa dónde revisar la salida.
            log.info("JOB FINALIZADO OK: revisar archivo output/clientes_modernos.csv");
        } else {
            // Si el Job falla o queda en otro estado, lo dejamos visible en consola.
            log.warn("JOB terminó con estado {}", jobExecution.getStatus());
        }
    }
}
