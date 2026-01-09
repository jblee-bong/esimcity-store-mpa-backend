package com.naver.naverspabackend.quartz.job;

import java.util.Date;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

@Getter
@Setter
@Slf4j
@DisallowConcurrentExecution // <- 작업 중복 실행을 막아주는 핵심 어노테이션
public class NaverQuartzJob extends QuartzJobBean {

    private String jobName;
    private JobLauncher jobLauncher;
    private JobLocator jobLocator;
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            jobLauncher = ((ApplicationContext) context.getScheduler().getContext().get("applicationContext")).getBean(JobLauncher.class);
            jobLocator = ((ApplicationContext) context.getScheduler().getContext().get("applicationContext")).getBean(JobLocator.class);
            Job job = jobLocator.getJob(jobName);
            JobDataMap jobDataMap = context.getTrigger().getJobDataMap();
            String jobTime = Objects.toString(jobDataMap.get("jobTime"), "0");
            JobParameters jobParameters = new JobParametersBuilder()
                .addString("jobTimeKey", jobTime)
                .addDate("requestTime", new Date())
                .toJobParameters();
            log.info("MyJob started. Thread name: {}", Thread.currentThread().getName());
            jobLauncher.run(job, jobParameters);
        } catch (Exception e) {
            log.error("exception", e);
            e.printStackTrace();
        }
    }
}