package com.backend.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(TavilyProperties.class)
public class TavilyConfiguration {

    @Bean
    public RestTemplate tavilyRestTemplate(TavilyProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(defaultTimeout(properties.connectTimeoutMillis(), 15000));
        factory.setReadTimeout(defaultTimeout(properties.readTimeoutMillis(), 30000));
        return new RestTemplate(factory);
    }

    private int defaultTimeout(Integer value, int fallback) {
        return value == null || value <= 0 ? fallback : value;
    }
}