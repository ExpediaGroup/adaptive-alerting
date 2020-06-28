package com.expedia.adaptivealerting.kafka.processor.openmetrics;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class OpenMetricRecord {
    private String metricName;
    private MetricType metricType;
    private String suffix;
    private String helpDescription;
    private String metricUnit;
    private Map<String, String> labelsMap;
    private Double value;
    private Double timestamp;
}
