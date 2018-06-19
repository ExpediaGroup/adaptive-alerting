package com.expedia.adaptivealerting.core.metric;

import com.expedia.www.haystack.commons.entities.MetricPoint;

public class MPoint {
    private Metric metric;
    private double value;
    private long epochSecond;

    public MPoint() {
    }

    public MPoint(MetricPoint metricPoint) {
        this.metric = new Metric(metricPoint);
        this.value = metricPoint.value();
        this.epochSecond = metricPoint.epochTimeInSeconds();
    }

    public Metric getMetric() {
        return metric;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public long getEpochSecond() {
        return epochSecond;
    }

    public void setEpochSecond(long epochSecond) {
        this.epochSecond = epochSecond;
    }
}
