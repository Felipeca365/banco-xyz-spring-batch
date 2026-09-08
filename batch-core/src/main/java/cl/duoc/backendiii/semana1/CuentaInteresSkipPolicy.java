package cl.duoc.backendiii.semana1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.file.FlatFileParseException;

public class CuentaInteresSkipPolicy implements SkipPolicy {

    private static final Logger log = LoggerFactory.getLogger(CuentaInteresSkipPolicy.class);
    private static final int LIMITE_OMISIONES = 5;

    @Override
    public boolean shouldSkip(Throwable throwable, long skipCount) throws SkipLimitExceededException {
        boolean errorDeFormato = throwable instanceof FlatFileParseException
                || throwable instanceof NumberFormatException;
        boolean dentroDelLimite = skipCount < LIMITE_OMISIONES;

        if (errorDeFormato && dentroDelLimite) {
            log.warn("Línea de intereses.csv omitida por error de formato (omisión {}/{}). Motivo: {}",
                    skipCount + 1, LIMITE_OMISIONES, throwable.getMessage());
            return true;
        }
        return false;
    }
}