package com.naver.naverspabackend.batch.reader;

import com.naver.naverspabackend.security.NaverRedisRepository;
import com.naver.naverspabackend.util.ApiUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NaverApiReader {

    @Autowired
    private NaverRedisRepository naverRedisRepository;

    @Autowired
    private ApiUtil apiUtil;


    @Value("${spring.profiles.active}")
    private String env;

    @Value("classpath:sample-data.json")
    private Resource sampleData;


}
