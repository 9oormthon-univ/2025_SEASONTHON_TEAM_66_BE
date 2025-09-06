package com.goormthon.careroad.nhis;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(NhisProperties.class)
public class NhisConfig {

    @Bean
    public RestClient nhisRestClient(NhisProperties props) {
        return RestClient.builder()
                .baseUrl(props.getBaseUrl() != null ? props.getBaseUrl() : "")
                .build();
    }
}
