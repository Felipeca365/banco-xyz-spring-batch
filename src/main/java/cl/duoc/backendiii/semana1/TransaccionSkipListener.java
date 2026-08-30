package cl.duoc.backendiii.semana1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;

public class TransaccionSkipListener implements SkipListener<Transaccion, TransaccionProcesada> {

    private static final Logger log = LoggerFactory.getLogger(TransaccionSkipListener.class);

    @Override
    public void onSkipInRead(Throwable t) {
        log.error("=== REGISTRO OMITIDO EN LECTURA === Motivo: {}", t.getMessage());
    }
}