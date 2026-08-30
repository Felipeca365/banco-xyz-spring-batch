# Banco XYZ — Modernización de Procesos Legacy con Spring Batch

Proyecto desarrollado para la asignatura **Desarrollo Backend III (PBY2203)**, DUOC UC, como parte de la actividad sumativa "Optimizando procesos batch para mejorar la resiliencia de procesos".

## 1. Objetivo del proyecto

Modernizar tres procesos batch del sistema legacy del **Banco XYZ**, originalmente ejecutados sobre archivos planos exportados desde un sistema COBOL, migrándolos a **Spring Batch** sobre una base de datos relacional (MySQL). El proyecto implementa:

- Lectura, validación y transformación de datos provenientes de archivos CSV con distintos niveles de calidad de datos (formatos inconsistentes, valores nulos, duplicados, tipos no documentados).
- Persistencia de los resultados en MySQL, conservando siempre el registro de anomalías detectadas (criterio de auditoría bancaria: nunca se descarta un dato sospechoso, se marca y se guarda).
- Tolerancia a fallos mediante políticas de reintento y omisión personalizadas.
- Dos estrategias distintas de escalamiento y procesamiento paralelo: **multi-threading** (Jobs 1 y 2) y **particionamiento** (Job 3).

## 2. Estructura del proyecto

```
src/main/java/cl/duoc/backendiii/semana1/
├── BatchConfiguration.java          # Configuración central: todos los @Bean (Jobs, Steps, Readers, Writers)
│
├── Job 1 — Transacciones diarias
│   ├── Transaccion.java
│   ├── TransaccionProcesada.java
│   ├── TransaccionProcessor.java
│   ├── TransaccionSkipPolicy.java
│   ├── TransaccionSkipListener.java
│   └── TransaccionJobCompletionListener.java
│
├── Job 2 — Intereses mensuales
│   ├── CuentaInteres.java
│   ├── CuentaInteresProcesada.java
│   ├── CuentaInteresProcessor.java
│   ├── CuentaInteresSkipPolicy.java
│   ├── CuentaInteresSkipListener.java
│   └── CuentaInteresJobCompletionListener.java
│
└── Job 3 — Estados de cuenta anuales
    ├── TransaccionAnual.java
    ├── TransaccionAnualProcesada.java
    ├── TransaccionAnualProcessor.java
    ├── EstadoCuentaAnualPartitioner.java
    └── EstadoCuentaAnualJobCompletionListener.java

src/main/resources/
├── application.properties
└── input/
    ├── transacciones.csv
    ├── intereses.csv
    └── cuentas_anuales.csv
```

## 3. Los tres Jobs

### Job 1 — Reporte de Transacciones Diarias

Lee `transacciones.csv`, detecta anomalías de negocio y las persiste en la tabla `transacciones`.

**Reglas de negocio (criterio propio, documentado):**
- Monto nulo, negativo o en cero → anomalía.
- Posible duplicado (misma fecha + monto + tipo) → anomalía.
- Errores de formato (fecha inválida, texto no numérico) → manejados con `SkipPolicy` (máximo 5 omisiones por ejecución).

**Escalamiento:** multi-threading, 3 hilos fijos (`corePoolSize=3, maxPoolSize=3`), con `SynchronizedItemStreamReader` para lectura concurrente segura sobre un único reader compartido.

### Job 2 — Cálculo de Intereses Mensuales

Lee `intereses.csv`, aplica una tasa de interés mensual según el tipo de cuenta y persiste el resultado en `cuentas_intereses`.

**Tasas aplicadas (criterio propio):**
- Ahorro: 0.5% mensual
- Préstamo: 1.5% mensual
- Hipoteca: 0.8% mensual
- Fórmula: `saldo_final = saldo + (saldo × tasa)`, redondeo HALF_UP a 2 decimales.

**Reglas de negocio:** saldo nulo, edad nula, tipo de cuenta inválido, edad fuera del rango 18-100 (inclusivo), posible duplicado → anomalía.

**Escalamiento:** igual estrategia que Job 1 (multi-threading, 3 hilos, reader sincronizado).

### Job 3 — Generación de Estados de Cuenta Anuales

A diferencia de los Jobs 1 y 2 (procesamiento fila a fila), este Job requiere **agregación**: compilar todas las transacciones del año de cada cuenta en un solo estado de cuenta resumen. Se implementó en **dos Steps**:

1. **`particionarTransaccionAnualStep`** (particionado en paralelo): lee `cuentas_anuales.csv` (1000 registros), normaliza y valida cada transacción, y escribe el detalle en `transacciones_anuales_detalle`.
2. **`generarEstadoCuentaAnualStep`** (tasklet secuencial): una vez que el Step anterior termina, ejecuta una agregación SQL (`GROUP BY cuenta_id`) y genera el resumen final en `estados_cuenta_anuales`.

**Reglas de negocio (criterio propio, dato de origen mucho más "sucio" que semana 1/2):**
- Fechas en 4 formatos distintos mezclados en el mismo archivo (`yyyy-MM-dd`, `yyyy/MM/dd`, `dd-MM-yyyy`, `dd/MM/yyyy`) → se intenta parsear en ese orden; si ninguno calza, anomalía.
- Tipo de transacción `depósito` (con tilde) → se normaliza a `deposito`, no es anomalía.
- Tipo `pago` (no contemplado en el enunciado original) → se decidió tratarlo como tipo válido, afecta el saldo igual que un retiro o compra.
- Monto vacío/nulo → anomalía.
- Depósito con monto negativo → anomalía (inconsistencia de negocio).
- Tipo de transacción no reconocido (fuera de depósito/retiro/compra/pago) → anomalía.
- Posible duplicado (misma cuenta + fecha + tipo + monto) → anomalía.
- Las anomalías se excluyen del cálculo del saldo neto, pero se cuentan en `cantidad_anomalias` para fines de auditoría.

**Escalamiento:** **particionamiento** (`Partitioner` + `TaskExecutorPartitionHandler`), con 4 particiones por defecto (`app.estado-anual.grid-size`), cada una con su propio `FlatFileItemReader` (`@StepScope`) operando sobre un rango exclusivo de filas — sin necesidad de sincronización entre particiones, a diferencia del enfoque de Job 1/2.

## 4. Decisiones de diseño transversales

- **Anomalías se marcan y conservan, nunca se descartan silenciosamente** (auditoría bancaria: nunca perder el rastro de un dato sospechoso).
- **Errores técnicos de formato** se manejan con `SkipPolicy`/`SkipListener` (Jobs 1 y 2); en Job 3 se decidió que el Reader nunca falle (todo se lee como `String`) y sea el Processor quien decida si algo es anomalía, dado el volumen y variedad de errores del dataset de esa semana.
- **Contadores del Processor** usan `AtomicInteger` y `ConcurrentHashMap.newKeySet()` para seguridad en concurrencia.
- **Lanzamiento de Jobs** vía `CommandLineRunner` manual con parámetro `timestamp` único por ejecución (evita un bug conocido de Spring Boot con `RunIdIncrementer` cuando ya existe una ejecución previa con parámetros vacíos).
- **Retry:** `TransientDataAccessException`, límite de 2 reintentos, pensado para fallos pasajeros de conexión a MySQL.

## 5. Requisitos para ejecutar

- Java 17+
- Apache Maven 3.9+
- MySQL 8.0 (local o remoto)

### 5.1 Base de datos

Crear el schema y las tablas necesarias:

```sql
CREATE DATABASE banco_xyz_batch CHARACTER SET utf8mb4;

USE banco_xyz_batch;

CREATE TABLE transacciones (
    id BIGINT PRIMARY KEY,
    fecha DATE,
    monto DECIMAL(15,2),
    tipo VARCHAR(20),
    es_anomalia BOOLEAN,
    motivo_anomalia VARCHAR(255)
);

CREATE TABLE cuentas_intereses (
    cuenta_id BIGINT PRIMARY KEY,
    nombre VARCHAR(100),
    saldo_original DECIMAL(15,2),
    saldo_final DECIMAL(15,2),
    edad INT,
    tipo VARCHAR(20),
    es_anomalia BOOLEAN,
    motivo_anomalia VARCHAR(255)
);

CREATE TABLE transacciones_anuales_detalle (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cuenta_id BIGINT,
    fecha DATE NULL,
    transaccion VARCHAR(20),
    monto DECIMAL(15,2) NULL,
    descripcion VARCHAR(255),
    es_anomalia BOOLEAN,
    motivo_anomalia VARCHAR(255)
);

CREATE TABLE estados_cuenta_anuales (
    cuenta_id BIGINT PRIMARY KEY,
    total_depositos DECIMAL(15,2),
    total_retiros_compras_pagos DECIMAL(15,2),
    saldo_neto_anual DECIMAL(15,2),
    cantidad_transacciones INT,
    cantidad_anomalias INT
);
```

### 5.2 Configuración de conexión

Ajustar `src/main/resources/application.properties` con las credenciales locales de MySQL (usuario, contraseña, puerto).

### 5.3 Ejecutar

```
mvn clean compile spring-boot:run
```

Los 3 Jobs se ejecutan automáticamente al arrancar la aplicación, en este orden: Job 3 (Estados de Cuenta Anuales), Job 1 (Transacciones Diarias), Job 2 (Intereses Mensuales). Cada uno imprime en consola un resumen final (total procesadas, válidas, anomalías).

**Importante:** antes de cada nueva ejecución de prueba, vaciar las tablas para evitar errores de llave duplicada:

```sql
TRUNCATE TABLE transacciones;
TRUNCATE TABLE cuentas_intereses;
TRUNCATE TABLE transacciones_anuales_detalle;
TRUNCATE TABLE estados_cuenta_anuales;
```

## 6. Evidencia de ejecución

Ver carpeta `/evidencias` en este repositorio: capturas de consola mostrando la ejecución exitosa de los 3 Jobs y consultas SQL verificando los resultados en cada tabla.

## 7. Autor

Proyecto desarrollado individualmente para DUOC UC — Analista Programador, Backend III (PBY2203), profesor Gabriel Grobier.
