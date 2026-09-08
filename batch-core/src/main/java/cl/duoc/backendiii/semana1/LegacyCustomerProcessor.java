package cl.duoc.backendiii.semana1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * ItemProcessor: transforma datos legacy en el formato moderno.
 *
 * Este es el componente más interesante para explicar en clase, porque aquí se
 * ve la lógica de negocio de la migración.
 *
 * En la guía de Semana 1 esto corresponde a la fase de transformación:
 * - normalizar textos,
 * - convertir fechas,
 * - convertir números,
 * - enriquecer/clasificar información,
 * - preparar los datos para el sistema destino.
 *
 * Tipo de entrada: LegacyCustomer
 * Tipo de salida: ModernCustomer
 */
public class LegacyCustomerProcessor implements ItemProcessor<LegacyCustomer, ModernCustomer> {

    private static final Logger log = LoggerFactory.getLogger(LegacyCustomerProcessor.class);

    /**
     * El sistema legacy entrega fechas como dd-MM-yyyy, por ejemplo 01-04-1992.
     * El sistema moderno las dejará como LocalDate, que luego se imprime yyyy-MM-dd.
     */
    private static final DateTimeFormatter LEGACY_DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    /**
     * Spring Batch llama a este método una vez por cada registro leído por el Reader.
     *
     * Si el Reader lee 4 clientes, este método se ejecuta 4 veces.
     * Cada ejecución recibe un LegacyCustomer y devuelve un ModernCustomer.
     */
    @Override
    public ModernCustomer process(LegacyCustomer item) {
        // 1. Normalizamos el identificador antiguo y le agregamos prefijo moderno.
        String customerId = "CLI-" + item.idLegacy().trim();

        // 2. Unimos nombre + apellido y normalizamos mayúsculas/minúsculas/espacios.
        String fullName = normalizeName(item.nombre()) + " " + normalizeName(item.apellido());

        // 3. Convertimos fecha desde texto legacy dd-MM-yyyy a LocalDate.
        LocalDate birthDate = LocalDate.parse(item.fechaNacimiento().trim(), LEGACY_DATE);

        // 4. Convertimos deuda desde texto a BigDecimal.
        // BigDecimal es mejor que double para montos porque evita errores de precisión.
        BigDecimal debtAmount = new BigDecimal(item.deudaPesos().trim());

        // 5. Normalizamos correo: trim + minúsculas + valor por defecto si viene vacío.
        String email = normalizeEmail(item.correo());

        // 6. Enriquecemos el dato clasificando el riesgo según deuda.
        String riskSegment = classifyRisk(debtAmount);

        // 7. Creamos el modelo moderno que será enviado al Writer.
        ModernCustomer result = new ModernCustomer(
                customerId,
                fullName,
                birthDate,
                debtAmount,
                email,
                riskSegment
        );

        // 8. Log didáctico: permite ver en consola cada transformación.
        log.info("Transformado {} -> {} ({})", item.idLegacy(), result.customerId(), result.riskSegment());
        return result;
    }

    /**
     * Normaliza nombres y apellidos.
     *
     * Ejemplos:
     * - "  ana " -> "Ana"
     * - " perez " -> "Perez"
     * - "MARÍA JOSÉ" -> "María José"
     *
     * Si el valor viene vacío, devuelve "Sin dato" para evitar campos nulos.
     */
    private String normalizeName(String value) {
        String clean = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        if (clean.isBlank()) {
            return "Sin dato";
        }

        String[] words = clean.split("\\s+");
        StringBuilder out = new StringBuilder();

        for (String word : words) {
            if (!out.isEmpty()) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }

        return out.toString();
    }

    /**
     * Normaliza correo.
     *
     * Reglas de este ejemplo didáctico:
     * - eliminar espacios al inicio/final,
     * - convertir a minúsculas,
     * - si viene vacío, usar un valor placeholder.
     *
     * En un caso real, aquí también podríamos validar formato y descartar registros
     * inválidos o enviarlos a un archivo de errores.
     */
    private String normalizeEmail(String value) {
        String clean = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        return clean.isBlank() ? "sin-correo@example.invalid" : clean;
    }

    /**
     * Clasifica riesgo según deuda.
     *
     * Regla simple de negocio para enriquecer el dato migrado:
     * - ALTO: deuda >= 100000
     * - MEDIO: deuda > 0 y menor que 100000
     * - BAJO: sin deuda
     */
    private String classifyRisk(BigDecimal debtAmount) {
        if (debtAmount.compareTo(new BigDecimal("100000")) >= 0) {
            return "ALTO";
        }
        if (debtAmount.compareTo(BigDecimal.ZERO) > 0) {
            return "MEDIO";
        }
        return "BAJO";
    }
}
