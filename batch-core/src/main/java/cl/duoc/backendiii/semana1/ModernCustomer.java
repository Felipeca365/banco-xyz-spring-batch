package cl.duoc.backendiii.semana1;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Modelo de salida: representa el cliente ya transformado al formato moderno.
 *
 * Este record es el resultado del ItemProcessor. A diferencia de LegacyCustomer,
 * aquí los datos ya están normalizados y listos para ser escritos por el writer.
 *
 * Comparación didáctica:
 *
 * LegacyCustomer:
 * - id: "0001"
 * - nombre: "  ana "
 * - apellido: " perez "
 * - fecha: "01-04-1992"
 * - correo: "ANA.PEREZ@CORREO.CL"
 *
 * ModernCustomer:
 * - customerId: "CLI-0001"
 * - fullName: "Ana Perez"
 * - birthDate: 1992-04-01
 * - email: "ana.perez@correo.cl"
 * - riskSegment: "MEDIO"
 *
 * Esta separación ayuda a explicar una idea central de Spring Batch:
 * el Reader lee un formato, el Processor lo convierte y el Writer escribe otro.
 */
public record ModernCustomer(
        String customerId,
        String fullName,
        LocalDate birthDate,
        BigDecimal debtAmount,
        String email,
        String riskSegment
) {
}
