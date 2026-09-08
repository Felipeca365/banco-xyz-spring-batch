package cl.duoc.backendiii.semana1;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

public class EstadoCuentaAnualJobCompletionListener implements JobExecutionListener {

    private final TransaccionAnualProcessor processor;

    public EstadoCuentaAnualJobCompletionListener(TransaccionAnualProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            System.out.println("=== Job 3: Estados de Cuenta Anuales ===");
            System.out.println("Total procesadas: " + processor.getTotalProcesadas());
            System.out.println("Válidas: " + processor.getTotalValidas());
            System.out.println("Anomalías: " + processor.getTotalAnomalias());
            System.out.println("=========================================");
        }
    }
}