package com.expedia.adaptivealerting.modelservice.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "security")
@Data
public class SecurityProperties {

    private String clientId;
    private String secret;
}