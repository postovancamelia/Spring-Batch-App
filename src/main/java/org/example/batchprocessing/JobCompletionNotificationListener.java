package org.example.batchprocessing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class JobCompletionNotificationListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            String outputFile = jobExecution.getJobParameters().getString("outputFile", "output-sector-accounts.csv");
            long read = 0, written = 0, filtered = 0;

            for (StepExecution se : jobExecution.getStepExecutions()) {
                read += se.getReadCount();
                written += se.getWriteCount();
                filtered += se.getFilterCount();
            }

            log.info("!!! JOB FINISHED");
            log.info("Output file: {}", outputFile);
            log.info("Items read: {}", read);
            log.info("Items written: {}", written);
            log.info("Items filtered (invalid): {}", filtered);
        }
    }
}
