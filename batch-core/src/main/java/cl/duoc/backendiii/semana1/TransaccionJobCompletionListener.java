package cl.duoc.backendiii.semana1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

public class TransaccionJobCompletionListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(TransaccionJobCompletionListener.class);

    private final TransaccionProcessor transaccionProcessor;

    public TransaccionJobCompletionListener(TransaccionProcessor transaccionProcessor) {
        this.transaccionProcessor = transaccionProcessor;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        int total = transaccionProcessor.getTotalProcesadas();
        int anomalias = transaccionProcessor.getTotalAnomalias();
        int validas = total - anomalias;

        log.info("========== RESUMEN JOB 1: TRANSACCIONES DIARIAS ==========");
        log.info("Estado final del Job: {}", jobExecution.getStatus());
        log.info("Total procesadas: {}", total);
        log.info("Transacciones válidas: {}", validas);
        log.info("Anomalías detectadas: {}", anomalias);
        log.info("============================================================");
    }
}