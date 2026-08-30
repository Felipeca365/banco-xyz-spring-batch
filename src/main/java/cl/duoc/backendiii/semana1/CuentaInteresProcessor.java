package cl.duoc.backendiii.semana1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CuentaInteresProcessor implements ItemProcessor<CuentaInteres, CuentaInteresProcesada> {

    private static final Logger log = LoggerFactory.getLogger(CuentaInteresProcessor.class);

    private static final int EDAD_MINIMA = 18;
    private static final int EDAD_MAXIMA = 100;

    // Tasas de interés mensual por tipo de cuenta (criterio propio, definido
    // para este proyecto académico, documentado en el informe).
    private static final BigDecimal TASA_AHORRO = new BigDecimal("0.005");
    private static final BigDecimal TASA_PRESTAMO = new BigDecimal("0.015");
    private static final BigDecimal TASA_HIPOTECA = new BigDecimal("0.008");

    private final Set<String> combinacionesVistas = ConcurrentHashMap.newKeySet();
    private final AtomicInteger totalProcesadas = new AtomicInteger(0);
    private final AtomicInteger totalAnomalias = new AtomicInteger(0);

    @Override
    public CuentaInteresProcesada process(CuentaInteres item) {
        totalProcesadas.incrementAndGet();

        log.info("{} procesando cuenta id={}", Thread.currentThread().getName(), item.getCuentaId());

        CuentaInteresProcesada procesada = new CuentaInteresProcesada();
        procesada.setCuentaId(item.getCuentaId());
        procesada.setNombre(item.getNombre());
        procesada.setSaldoOriginal(item.getSaldo());
        procesada.setEdad(item.getEdad());
        procesada.setTipo(item.getTipo());

        // Regla 1: saldo faltante o ilegible.
        if (item.getSaldo() == null) {
            return marcarAnomalia(procesada, "Saldo faltante o ilegible");
        }

        // Regla 2: edad faltante.
        if (item.getEdad() == null) {
            return marcarAnomalia(procesada, "Edad faltante");
        }

        // Regla 3: tipo de cuenta inválido (no es ahorro, prestamo ni hipoteca).
        BigDecimal tasa = obtenerTasa(item.getTipo());
        if (tasa == null) {
            return marcarAnomalia(procesada, "Tipo de cuenta inválido");
        }

        // Regla 4: edad fuera de un rango realista.
        if (item.getEdad() < EDAD_MINIMA || item.getEdad() > EDAD_MAXIMA) {
            return marcarAnomalia(procesada, "Edad fuera de rango válido");
        }

        // Regla 5: posible cuenta duplicada (mismo nombre, saldo, edad y tipo ya vistos).
        String clave = item.getNombre() + "|" + item.getSaldo() + "|" + item.getEdad() + "|" + item.getTipo();
        if (!combinacionesVistas.add(clave)) {
            return marcarAnomalia(procesada, "Posible cuenta duplicada");
        }

        // Si pasó todas las reglas: calculamos el interés y el saldo final.
        BigDecimal interes = item.getSaldo().multiply(tasa);
        BigDecimal saldoFinal = item.getSaldo().add(interes).setScale(2, RoundingMode.HALF_UP);

        procesada.setSaldoFinal(saldoFinal);
        procesada.setEsAnomalia(false);
        procesada.setMotivoAnomalia(null);
        return procesada;
    }

    private CuentaInteresProcesada marcarAnomalia(CuentaInteresProcesada procesada, String motivo) {
        procesada.setEsAnomalia(true);
        procesada.setMotivoAnomalia(motivo);
        procesada.setSaldoFinal(null);
        totalAnomalias.incrementAndGet();
        return procesada;
    }

    private BigDecimal obtenerTasa(String tipo) {
        if (tipo == null) {
            return null;
        }
        return switch (tipo.trim().toLowerCase()) {
            case "ahorro" -> TASA_AHORRO;
            case "prestamo" -> TASA_PRESTAMO;
            case "hipoteca" -> TASA_HIPOTECA;
            default -> null;
        };
    }

    public int getTotalProcesadas() {
        return totalProcesadas.get();
    }

    public int getTotalAnomalias() {
        return totalAnomalias.get();
    }
}