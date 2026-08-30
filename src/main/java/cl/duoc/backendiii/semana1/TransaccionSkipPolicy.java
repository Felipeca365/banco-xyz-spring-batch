package cl.duoc.backendiii.semana1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.file.FlatFileParseException;

public class TransaccionSkipPolicy implements SkipPolicy {

    private static final Logger log= LoggerFactory.getLogger(TransaccionSkipPolicy.class);

    //Maximo de lineas corruptas que toleramos en ejecucion.
    // Si se supera, asumimos que el archivo completo esta mal y detenemos al Job
    // en vez de seguir omitiendo silenciosamente cada vez mas filas.
    private static final int LIMITE_OMISIONES = 5;

    @Override
    public boolean shouldSkip(Throwable throwable, long skipCount) throws SkipLimitExceededException {
        boolean errorDeFormato = throwable instanceof FlatFileParseException;
        boolean dentroDelLimite = skipCount < LIMITE_OMISIONES;

        if (errorDeFormato && dentroDelLimite) {
            log.warn("Linea del CSV omitida por error de formato (omisión {}/{}). motivo: {}",
                skipCount + 1, LIMITE_OMISIONES, throwable.getMessage());
            return true;
        
        }
        return false;
    }

}