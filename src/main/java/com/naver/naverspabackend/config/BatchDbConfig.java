package com.naver.naverspabackend.config;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(value = "com.naver.naverspabackend.mybatis.batchmapper" , sqlSessionTemplateRef = "batchDbSessionTemplate")
public class BatchDbConfig {

    @Bean
    @Qualifier("batchDbSessionTemplate")
    public SqlSessionTemplate batchDbSessionTemplate(SqlSessionFactory sqlSessionFactory){
        return new SqlSessionTemplate(sqlSessionFactory, ExecutorType.BATCH);
    }

}
