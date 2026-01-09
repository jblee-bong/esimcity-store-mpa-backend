package com.naver.naverspabackend.batch.config;

import com.naver.naverspabackend.batch.listener.JobCompletionNotificationListener;
import com.naver.naverspabackend.batch.tasklet.*;
import com.naver.naverspabackend.quartz.job.NaverQuartzJob;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
@Slf4j
public class BatchConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final JobExplorer jobExplorer;
    private final JobRegistry jobRegistry;
    private final JobLauncher jobLauncher;
    private final JobRepository jobRepository;

    @Bean
    public Job importNaverJob(JobCompletionNotificationListener listener,
        @Qualifier("naverMainStep") Step naverMainStep,
        @Qualifier("naverOrderStep") Step naverOrderStep,
        @Qualifier("naverOrderingStatusStep") Step naverOrderingStatusStep,
        @Qualifier("naverSmsStep") Step naverSmsStep,
        @Qualifier("naverCancelIccidStep") Step naverCancelIccidStep,
        @Qualifier("naverChangeStatusStep") Step naverChangeStatusStep
       ) {
        return jobBuilderFactory.get("importNaverJob")
            .incrementer(new RunIdIncrementer())
            .listener(listener)
            .start(naverMainStep)
            .next(naverOrderStep)
            .next(naverOrderingStatusStep)
            .next(naverSmsStep)
            .next(naverCancelIccidStep)
            .next(naverChangeStatusStep)
            .build();
    }

    /**
     * 메인 DB에서 Naver 을 읽어와 상품 검색 후 write 하는 Step
     *
     * @return
     */
    @Bean
    public Step naverMainStep(
        NaverProductTasklet naverMainTasklet
    ) {
        return stepBuilderFactory.get("naverMainStep")
            .tasklet(naverMainTasklet)
            .build();
    }

    /**
     * 메인 DB에서 Naver 을 읽어와 order 검색 후 write 하는 Step
     *
     * @return
     */
    @Bean
    public Step naverOrderStep(
        NaverOrderTasklet naverOrderTasklet
    ) {
        return stepBuilderFactory.get("naverOrderStep")
            .tasklet(naverOrderTasklet)
            .build();
    }

    @Bean
    public Step naverOrderingStatusStep(
            NaverOrderingStatusTasklet naverOrderingStatusTasklet
    ) {
        return stepBuilderFactory.get("naverOrderingStatusStep")
                .tasklet(naverOrderingStatusTasklet)
                .build();
    }

    /**
     * 메인 DB에서 Naver 을 읽어와 order 검색 후 sms send Step
     *
     * @return
     */
    @Bean
    public Step naverSmsStep(
        NaverSmsTasklet naverSmsTasklet
    ) {
        return stepBuilderFactory.get("naverSmsStep")
            .tasklet(naverSmsTasklet)
            .build();
    }

    /**
     * 메인 DB에서 Naver 을 읽어와 order 검색 후 취소하였으나 구매한건에관하여 iccid 업데이트
     *
     * @return
     */
    @Bean
    public Step naverCancelIccidStep(
            NaverCancelIccidTasklet naverCancelIccidTasklet
    ) {
        return stepBuilderFactory.get("naverCancelIccidStep")
                .tasklet(naverCancelIccidTasklet)
                .build();
    }


    /**
     * 메인 DB에서 Order 를 읽어와 발송처리 변경을 요청하는 경우 변경 change status Step
     *
     * @return
     */
    @Bean
    public Step naverChangeStatusStep(
            NaverChangeStatusTasklet naverChangeStatusTasklet
    ) {
        return stepBuilderFactory.get("naverChangeStatusStep")
                .tasklet(naverChangeStatusTasklet)
                .build();
    }



    @Bean
    public JobOperator jobOperator() {
        SimpleJobOperator jobOperator = new SimpleJobOperator();
        jobOperator.setJobExplorer(jobExplorer);
        jobOperator.setJobLauncher(jobLauncher);
        jobOperator.setJobRegistry(jobRegistry);
        jobOperator.setJobRepository(jobRepository);
        return jobOperator;
    }

    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor() {
        JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
        jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);
        return jobRegistryBeanPostProcessor;
    }
    @Bean
    @Qualifier("quartzJobDetail")
    public JobDetailFactoryBean quartzJobDetail() {
        JobDetailFactoryBean factory = new JobDetailFactoryBean();
        factory.setJobClass(NaverQuartzJob.class);
        factory.setDurability(true);
        Map<String, Object> map = new HashMap<>();
        map.put("jobName", "importNaverJob");
        factory.setJobDataAsMap(map);
        return factory;
    }

}
