package com.expedia.adaptivealerting.kafka.processor.openmetrics;

import java.util.HashMap;
import java.util.Map;

public enum MetricType {
    COUNTER("counter"),
    INFO("info"),
    GAUGE("gauge"),
    HISTOGRAM("histogram"),
    UNKNOWN("unknown"),
    SUMMARY("summary");

    private String type;

    static Map<String, MetricType> values = new HashMap<>();

    MetricType(String type) {
        this.type = type;
    }

    static {
        for(MetricType metricType : MetricType.values()){
            values.put(metricType.type, metricType);
        }
    }

    public String getType() {
        return this.type;
    }

    public static MetricType getByType(String type) {
        return values.get(type);
    }
}
