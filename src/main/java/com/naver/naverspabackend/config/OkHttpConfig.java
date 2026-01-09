package com.naver.naverspabackend.config;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
@Slf4j
public class OkHttpConfig {

    @Bean
    public HttpClient httpClient() {
        return HttpClientBuilder.create()
            .setMaxConnTotal(100)    //최대 오픈되는 커넥션 수, 연결을 유지할 최대 숫자
            .setMaxConnPerRoute(30)   //IP, 포트 1쌍에 대해 수행할 커넥션 수, 특정 경로당 최대 숫자
            .build();
    }

    @Bean
    public HttpComponentsClientHttpRequestFactory factory(HttpClient httpClient) {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(60000);
        factory.setConnectTimeout(10000);
        factory.setHttpClient(httpClient);

        return factory;
    }

    @Bean
    public RestTemplate restTemplate(HttpComponentsClientHttpRequestFactory factory) {
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
    }

    @Bean
    public OkHttpClient okHttpClient(){
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(10000, TimeUnit.MILLISECONDS)
            .readTimeout(60000, TimeUnit.MILLISECONDS)
            .build();
        return okHttpClient;
    }
}
