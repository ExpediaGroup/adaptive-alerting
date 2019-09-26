package com.expedia.adaptivealerting.metrics.functions.sink;

import com.expedia.metrics.MetricData;

public interface MetricFunctionsPublish {

    public void initPublisher();

    public void publishMetrics(MetricData metricData);
}
