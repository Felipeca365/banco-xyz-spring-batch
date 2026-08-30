package cl.duoc.backendiii.semana1;

/**
 * Modelo de entrada: representa un registro tal como viene desde el sistema legacy.
 *
 * En una migración real, este tipo de estructura suele venir desde:
 * - archivos planos exportados por sistemas antiguos,
 * - reportes COBOL,
 * - scripts shell,
 * - bases de datos heredadas,
 * - integraciones antiguas.
 *
 * Este record NO representa todavía el formato correcto del sistema nuevo.
 * Representa el dato "sucio" o "heredado" que necesitamos transformar.
 *
 * Campos del CSV legacy:
 * - idLegacy: identificador antiguo, por ejemplo "0001".
 * - nombre: nombre con posibles espacios o formato inconsistente.
 * - apellido: apellido con posibles espacios o formato inconsistente.
 * - fechaNacimiento: fecha en formato legacy dd-MM-yyyy.
 * - deudaPesos: deuda como texto, porque viene desde archivo plano.
 * - correo: correo con posibles mayúsculas o vacío.
 *
 * Nota para clase:
 * Usamos record porque es una forma simple e inmutable de modelar datos en Java.
 */
public record LegacyCustomer(
        String idLegacy,
        String nombre,
        String apellido,
        String fechaNacimiento,
        String deudaPesos,
        String correo
) {
}
