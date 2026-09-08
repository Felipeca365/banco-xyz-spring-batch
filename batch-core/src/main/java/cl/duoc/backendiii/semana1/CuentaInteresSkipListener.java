package cl.duoc.backendiii.semana1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;

public class CuentaInteresSkipListener implements SkipListener<CuentaInteres, CuentaInteresProcesada> {

    private static final Logger log = LoggerFactory.getLogger(CuentaInteresSkipListener.class);

    @Override
    public void onSkipInRead(Throwable t) {
        log.error("=== CUENTA OMITIDA EN LECTURA === Motivo: {}", t.getMessage());
    }
}