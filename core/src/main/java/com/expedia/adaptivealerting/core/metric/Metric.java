package com.expedia.adaptivealerting.core.metric;

import com.expedia.www.haystack.commons.entities.MetricPoint;
import scala.Enumeration;
import scala.collection.JavaConverters;

import java.util.Map;

public class Metric {
    private String name;
    private String type;
    private Map<String, String> tags;

    public Metric() {
    }

    public Metric(MetricPoint metricPoint) {
        this.name = metricPoint.metric();
        this.type = metricPoint.type().toString(); // scala enums are difficult to deserialize
        this.tags = JavaConverters.mapAsJavaMap(metricPoint.tags());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
}
