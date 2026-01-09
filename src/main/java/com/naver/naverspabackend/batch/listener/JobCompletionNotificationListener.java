package com.naver.naverspabackend.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.stereotype.Service;

@Service
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {

    private static final Logger LOG = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

    @Override
    public void beforeJob(JobExecution jobExecution) {
        LOG.info("### JOB {}!!!", jobExecution.getStatus());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        LOG.info("### JOB {}!!!", jobExecution.getStatus());
    }
}
