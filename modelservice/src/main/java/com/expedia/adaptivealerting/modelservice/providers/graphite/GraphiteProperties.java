package com.expedia.adaptivealerting.modelservice.providers.graphite;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author kashah
 */
@Data
@Component
@ConfigurationProperties(prefix = "graphite")
public class GraphiteProperties {
    private String urlTemplate;
}