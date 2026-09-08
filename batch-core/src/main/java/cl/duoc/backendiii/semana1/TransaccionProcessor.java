package cl.duoc.backendiii.semana1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TransaccionProcessor implements ItemProcessor<Transaccion, TransaccionProcesada> {

    private static final Logger log = LoggerFactory.getLogger(TransaccionProcessor.class);

    // ConcurrentHashMap.newKeySet() es un Set seguro para que varios hilos
    // lean y escriban en él al mismo tiempo, sin corromper sus datos internos.
    private final Set<String> combinacionesVistas = ConcurrentHashMap.newKeySet();

    // AtomicInteger permite incrementar un número de forma segura entre hilos,
    // a diferencia de un int normal, donde dos hilos podrían "pisarse".
    private final AtomicInteger totalProcesadas = new AtomicInteger(0);
    private final AtomicInteger totalAnomalias = new AtomicInteger(0);

    @Override
    public TransaccionProcesada process(Transaccion item) {
        totalProcesadas.incrementAndGet();

        log.info("{} procesando transacción id={}", Thread.currentThread().getName(), item.getId());

        TransaccionProcesada procesada = new TransaccionProcesada();
        procesada.setId(item.getId());
        procesada.setFecha(item.getFecha());
        procesada.setMonto(item.getMonto());
        procesada.setTipo(item.getTipo());

        // Regla 1: monto ilegible o ausente.
        if (item.getMonto() == null) {
            procesada.setEsAnomalia(true);
            procesada.setMotivoAnomalia("Monto faltante o ilegible");
            totalAnomalias.incrementAndGet();
            return procesada;
        }

        // Regla 2: monto negativo.
        if (item.getMonto().compareTo(BigDecimal.ZERO) < 0) {
            procesada.setEsAnomalia(true);
            procesada.setMotivoAnomalia("Monto negativo");
            totalAnomalias.incrementAndGet();
            return procesada;
        }

        // Regla 3: monto en cero.
        if (item.getMonto().compareTo(BigDecimal.ZERO) == 0) {
            procesada.setEsAnomalia(true);
            procesada.setMotivoAnomalia("Monto en cero");
            totalAnomalias.incrementAndGet();
            return procesada;
        }

        // Regla 4: posible duplicado (misma fecha, monto y tipo ya vistos antes).
        String clave = item.getFecha() + "|" + item.getMonto() + "|" + item.getTipo();
        if (!combinacionesVistas.add(clave)) {
            procesada.setEsAnomalia(true);
            procesada.setMotivoAnomalia("Posible transacción duplicada (mismo monto, fecha y tipo)");
            totalAnomalias.incrementAndGet();
            return procesada;
        }

        // Si pasó todas las reglas, la transacción es válida.
        procesada.setEsAnomalia(false);
        procesada.setMotivoAnomalia(null);
        return procesada;
    }

    public int getTotalProcesadas() {
        return totalProcesadas.get();
    }

    public int getTotalAnomalias() {
        return totalAnomalias.get();
    }
}