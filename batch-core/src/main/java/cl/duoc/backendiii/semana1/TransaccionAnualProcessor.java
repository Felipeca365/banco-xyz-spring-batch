package cl.duoc.backendiii.semana1;

import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Processor del Job 3 (Estados de Cuenta Anuales).
 *
 * Recibe TransaccionAnual (todo en String, crudo) y devuelve
 * TransaccionAnualProcesada (ya tipado, con anomalías marcadas).
 *
 * Al igual que en TransaccionProcessor y CuentaInteresProcessor,
 * las anomalías se MARCAN y se CONSERVAN, nunca se descartan
 * silenciosamente (criterio de auditoría bancaria).
 *
 * Como este Processor corre en paralelo (varias particiones a la
 * vez), los contadores usan AtomicInteger y no int normal.
 */
public class TransaccionAnualProcessor implements ItemProcessor<TransaccionAnual, TransaccionAnualProcesada> {

    private static final DateTimeFormatter[] FORMATOS_FECHA = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
    };

    private static final Set<String> TIPOS_VALIDOS = Set.of("deposito", "retiro", "compra", "pago");

    private final AtomicInteger totalProcesadas = new AtomicInteger(0);
    private final AtomicInteger totalValidas = new AtomicInteger(0);
    private final AtomicInteger totalAnomalias = new AtomicInteger(0);
    private final Set<String> combinacionesVistas = ConcurrentHashMap.newKeySet();

    @Override
    public TransaccionAnualProcesada process(TransaccionAnual item) {
        totalProcesadas.incrementAndGet();

        TransaccionAnualProcesada resultado = new TransaccionAnualProcesada();
        resultado.setCuentaId(item.getCuentaId());
        resultado.setDescripcion(item.getDescripcion());

        boolean esAnomalia = false;
        StringBuilder motivo = new StringBuilder();

        // Regla 1: fecha
        LocalDate fecha = parsearFecha(item.getFecha());
        resultado.setFecha(fecha);
        if (fecha == null) {
            esAnomalia = true;
            motivo.append("Fecha inválida o formato no reconocido. ");
        }

        // Regla 2: tipo de transacción
        String tipo = normalizarTipo(item.getTransaccion());
        resultado.setTransaccion(tipo);
        if (tipo == null || !TIPOS_VALIDOS.contains(tipo)) {
            esAnomalia = true;
            motivo.append("Tipo de transacción no reconocido: '").append(item.getTransaccion()).append("'. ");
        }

        // Regla 3: monto vacío o nulo
        BigDecimal monto = parsearMonto(item.getMonto());
        resultado.setMonto(monto);
        if (monto == null) {
            esAnomalia = true;
            motivo.append("Monto nulo o vacío. ");
        }

        // Regla 4: monto negativo en un depósito (inconsistencia de negocio)
        if (monto != null && "deposito".equals(tipo) && monto.compareTo(BigDecimal.ZERO) < 0) {
            esAnomalia = true;
            motivo.append("Depósito con monto negativo. ");
        }

        // Regla 5: posible duplicado (misma cuenta + fecha + tipo + monto)
        if (fecha != null && tipo != null && monto != null) {
            String clave = item.getCuentaId() + "|" + fecha + "|" + tipo + "|" + monto;
            if (!combinacionesVistas.add(clave)) {
                esAnomalia = true;
                motivo.append("Posible transacción duplicada. ");
            }
        }

        resultado.setEsAnomalia(esAnomalia);
        resultado.setMotivoAnomalia(esAnomalia ? motivo.toString().trim() : null);

        if (esAnomalia) {
            totalAnomalias.incrementAndGet();
        } else {
            totalValidas.incrementAndGet();
        }

        return resultado;
    }

    private LocalDate parsearFecha(String fechaTexto) {
        if (fechaTexto == null || fechaTexto.trim().isEmpty()) {
            return null;
        }
        String texto = fechaTexto.trim();
        for (DateTimeFormatter formato : FORMATOS_FECHA) {
            try {
                return LocalDate.parse(texto, formato);
            } catch (DateTimeParseException e) {
                // probamos el siguiente formato
            }
        }
        return null;
    }

    private String normalizarTipo(String tipoTexto) {
        if (tipoTexto == null) {
            return null;
        }
        String limpio = tipoTexto.trim().toLowerCase();
        if (limpio.equals("depósito")) {
            return "deposito";
        }
        return limpio;
    }

    private BigDecimal parsearMonto(String montoTexto) {
        if (montoTexto == null || montoTexto.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(montoTexto.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public int getTotalProcesadas() {
        return totalProcesadas.get();
    }

    public int getTotalValidas() {
        return totalValidas.get();
    }

    public int getTotalAnomalias() {
        return totalAnomalias.get();
    }
}