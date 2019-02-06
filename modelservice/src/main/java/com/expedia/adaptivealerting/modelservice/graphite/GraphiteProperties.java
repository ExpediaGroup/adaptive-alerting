package com.expedia.adaptivealerting.modelservice.graphite;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "graphite")
@Data
public class GraphiteProperties {
    private String urlTemplate;
}
