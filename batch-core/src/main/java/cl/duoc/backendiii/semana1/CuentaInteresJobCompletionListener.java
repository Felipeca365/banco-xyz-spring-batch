package cl.duoc.backendiii.semana1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

public class CuentaInteresJobCompletionListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(CuentaInteresJobCompletionListener.class);
    private final CuentaInteresProcessor cuentaInteresProcessor;

    public CuentaInteresJobCompletionListener(CuentaInteresProcessor cuentaInteresProcessor) {
        this.cuentaInteresProcessor = cuentaInteresProcessor;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        int total = cuentaInteresProcessor.getTotalProcesadas();
        int anomalias = cuentaInteresProcessor.getTotalAnomalias();
        int validas = total - anomalias;

        log.info("========== RESUMEN JOB 2: INTERESES MENSUALES ==========");
        log.info("Estado final del Job: {}", jobExecution.getStatus());
        log.info("Total procesadas: {}", total);
        log.info("Cuentas válidas (interés aplicado): {}", validas);
        log.info("Anomalías detectadas: {}", anomalias);
        log.info("==========================================================");
    }
}