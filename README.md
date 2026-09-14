# Banco XYZ - Sistema Backend for Frontend (BFF)

**Autor:** Felipe Cabrera
**Asignatura:** Desarrollo Backend III (PBY2203) - DUOC UC
**Actividad:** Exp2 - Semana 5 - Implementando el patrón arquitectónico Backend for Frontend (BFF)

## Objetivo del proyecto

Este proyecto implementa el patrón arquitectónico **Backend for Frontend (BFF)** sobre el sistema bancario Banco XYZ, con el fin de optimizar la comunicación entre distintos tipos de clientes (web, móvil y cajero automático) y los datos del banco, generados previamente mediante un proceso batch con Spring Batch (Jobs de transacciones, intereses y estados de cuenta anuales).

Cada canal cuenta con su propio backend independiente, con respuestas personalizadas según sus necesidades, y con un mecanismo de autenticación propio y diferenciado.

## Estrategia de implementación elegida

Se optó por la estrategia de **Backends independientes por cada tipo de cliente**, en lugar de endpoints personalizados sobre un único backend o delegación por microservicios. Esta decisión se sustenta en:

- La necesidad de reglas de negocio y seguridad completamente distintas por canal (Basic Auth para Web, JWT para Móvil, token propio para Cajero).
- La naturaleza física y crítica del canal Cajero, que exige aislamiento de seguridad respecto a los demás canales.
- El carácter académico del ejercicio, que buscaba demostrar el patrón BFF de forma explícita y completa.

### Relación con el ejemplo de refactorización de monolito de la guía

La guía de la semana 5 ilustra el proceso de refactorización mediante un caso genérico: un método `getData(clientType)` que decide su comportamiento con una sentencia `if` según el tipo de cliente, refactorizado en dos métodos independientes bajo la estrategia de endpoints personalizados.

Este proyecto aplica el mismo principio de fondo -eliminar la bifurcación condicional por tipo de cliente- pero llevándolo un paso más allá: en lugar de separar únicamente las rutas dentro de una misma aplicación, se optó desde el diseño inicial por la estrategia de backends independientes. Cada canal (`bff-web`, `bff-movil`, `bff-cajero`) es un módulo Maven y un proceso Spring Boot completamente separado, con su propio punto de entrada, su propio mecanismo de autenticación y su propio ciclo de despliegue. En ningún punto del código existe una condición del tipo `if (clientType.equals("web"))`: la decisión de qué lógica ejecutar no ocurre en tiempo de ejecución dentro de un método compartido, sino en tiempo de diseño, al enrutar cada cliente directamente hacia su propio servicio.

## Arquitectura del proyecto

Proyecto Maven multi-módulo:

desarrollo_Backend3_s1-main/
|-- pom.xml (POM padre agregador)
|-- batch-core/ (Jobs de Spring Batch: transacciones, intereses, estados anuales)
|-- common-data/ (Entidades JPA y repositorios compartidos por los 3 BFF)
|-- bff-web/ (BFF para clientes web)
|-- bff-movil/ (BFF para clientes moviles)
|-- bff-cajero/ (BFF para cajeros automaticos)
|-- keystore.p12 (Certificado autofirmado compartido para HTTPS)
`-- evidencias/ (Capturas de ejecución de cada API)


## Detalle de cada BFF

### BFF Web (puerto 8081)

- Expone los datos completos de cada entidad (transacciones, cuentas, estados anuales, detalle anual).
- Autenticación: **HTTP Basic Auth** (usuario y clave fijos).
- Endpoints principales:
  - `GET /api/web/transacciones`
  - `GET /api/web/cuentas`
  - `GET /api/web/estados-anuales`
  - `GET /api/web/transacciones-anuales`

### BFF Movil (puerto 8082)

- Expone DTOs livianos, con solo los campos esenciales, reduciendo el tamaño de las respuestas entre un 60% y un 70% respecto al BFF Web.
- Autenticación: **JWT (JSON Web Token)**, mediante login previo.
- Endpoints principales:
  - `POST /api/movil/auth/login` (obtiene el token)
  - `GET /api/movil/transacciones`
  - `GET /api/movil/cuentas`
  - `GET /api/movil/estados-anuales`

### BFF Cajero (puerto 8083)

- Expone únicamente los datos mínimos necesarios para operar en un cajero físico (saldo y resumen anual).
- Autenticación: **token fijo propio**, enviado en el header `X-Cajero-Token`.
- Endpoints principales:
  - `GET /api/cajero/saldo/{cuentaId}`
  - `GET /api/cajero/estado-anual/{cuentaId}`

## Seguridad implementada

- **HTTPS** habilitado en los 3 BFF mediante certificado autofirmado (`keystore.p12`, formato PKCS12).
- **Autenticación diferenciada por canal**, según se detalla arriba.
- Cada canal valida sus credenciales mediante un `HandlerInterceptor` propio, sin lógica compartida entre BFF.

## Requisitos previos

- Java 17
- Maven 3.9+
- MySQL 8 con la base `banco_xyz_batch` ya poblada (ejecutar previamente los Jobs de `batch-core`)
- Variables de entorno configuradas: `DB_USERNAME`, `DB_PASSWORD`

## Instrucciones de ejecución

### 1. Compilar el proyecto completo

mvn clean install


### 2. Levantar cada BFF (en terminales separadas)

cd bff-web
mvn spring-boot:run

cd bff-movil
mvn spring-boot:run

cd bff-cajero
mvn spring-boot:run


### 3. Probar los endpoints

**BFF Web (Basic Auth):**

curl -Credential (usuario: admin.web / clave: WebXYZ2026!) https://localhost:8081/api/web/transacciones


**BFF Movil (JWT):**

POST https://localhost:8082/api/movil/auth/login
Body: {"usuario":"app.movil","clave":"MovilXYZ2026!"}

GET https://localhost:8082/api/movil/transacciones
Header: Authorization: Bearer <token obtenido>


**BFF Cajero (token fijo):**

GET https://localhost:8083/api/cajero/saldo/101
Header: X-Cajero-Token: CAJERO-XYZ-2026-SECRETO


Nota: al usar un certificado autofirmado, los clientes HTTP deben configurarse para aceptar certificados no verificados en el entorno de desarrollo (no aplica en producción).

## Evidencias

Las capturas de ejecución de cada API, incluyendo los casos de éxito (200 OK) y de rechazo por falta de autenticación (401), se encuentran en la carpeta `evidencias/`.