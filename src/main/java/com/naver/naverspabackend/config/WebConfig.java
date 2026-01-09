package com.naver.naverspabackend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naver.naverspabackend.filter.RequestInitializeFilter;
import com.naver.naverspabackend.resolver.PagingResolver;
import com.naver.naverspabackend.resolver.UserDecodeResolver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final UserDecodeResolver userDecodeResolver;
    private final PagingResolver pageResolver;


    private final ObjectMapper objectMapper;

    @Value("${naver.encodingType}")
    private String encodingType;

    @Bean
    public FilterRegistrationBean requestInitializeFilterRegistrationBean() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new RequestInitializeFilter());
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.setOrder(1);
        return filterRegistrationBean;
    }


    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userDecodeResolver);
        resolvers.add(pageResolver);
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**").addResourceLocations("classpath:/upload/resources/");
    }

}
