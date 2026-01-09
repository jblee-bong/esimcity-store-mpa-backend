package com.naver.naverspabackend.quartz.config;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class QuartzConfig {

    private final DataSource dataSource;

    @Autowired
    @Qualifier("quartzJobDetail")
    private JobDetailFactoryBean quartzJobDetail;


    @Value("classpath:quartz.properties")
    private Resource quartzPropertiesResource;

    @Value("${spring.profiles.active}")
    private String env;

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() throws IOException {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();

        if("local".equals(env) || "dev".equals(env)){
            factory.setTriggers(
                quartzJobTriggerProductSearch().getObject()
            );
        }else{
            factory.setTriggers(
                quartzJobTriggerProductSearch().getObject()
            );
        }
        Properties properties = PropertiesLoaderUtils.loadProperties(quartzPropertiesResource);
        factory.setDataSource(dataSource);
        factory.setQuartzProperties(properties);
        factory.setApplicationContextSchedulerContextKey("applicationContext");
        return factory;
    }

    @Bean
    public CronTriggerFactoryBean quartzJobTriggerProductSearch() {
        CronTriggerFactoryBean factory = new CronTriggerFactoryBean();
        factory.setJobDetail(quartzJobDetail.getObject());
        factory.setStartDelay(0);
        factory.setName("상품 검색");
        factory.setMisfireInstructionName("MISFIRE_INSTRUCTION_DO_NOTHING"); //제때 실행되지 못한(밀린) 작업들은 그냥 무시해라는설정
        factory.setCronExpression("0 * * * * ?");//매 1분 실행되도록하고, DisallowConcurrentExecution 이어노테이션을 통해 종료 후 실행되도록 함
        Map<String, Object> jobDataMap = new HashMap<>();
        jobDataMap.put("jobTime", "0");
        factory.setJobDataAsMap(jobDataMap);
        return factory;
    }

}
