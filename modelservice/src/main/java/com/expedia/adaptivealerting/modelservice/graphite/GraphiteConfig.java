package com.expedia.adaptivealerting.modelservice.graphite;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GraphiteConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
