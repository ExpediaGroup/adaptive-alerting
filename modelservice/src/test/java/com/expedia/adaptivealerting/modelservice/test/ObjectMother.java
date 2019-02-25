package com.expedia.adaptivealerting.modelservice.test;

import com.expedia.adaptivealerting.modelservice.entity.Metric;
import com.expedia.adaptivealerting.modelservice.providers.graphite.GraphiteResult;
import com.expedia.adaptivealerting.modelservice.providers.graphite.Tags;
import com.expedia.adaptivealerting.modelservice.service.AnomalyRequest;
import com.expedia.adaptivealerting.modelservice.spi.MetricSource;
import com.expedia.adaptivealerting.modelservice.spi.MetricSourceResult;

import java.util.HashMap;
import java.util.Map;

public class ObjectMother {
    private static final ObjectMother MOM = new ObjectMother();

    public static ObjectMother instance() {
        return MOM;
    }

    private ObjectMother() {
    }

    public GraphiteResult[] getGraphiteData() {
        GraphiteResult[] results = new GraphiteResult[1];
        GraphiteResult result = new GraphiteResult();
        String[][] datapoints = new String[2][2];
        datapoints[0][0] = "78.0";
        datapoints[0][1] = "1548829800";
        datapoints[1][0] = "81.0";
        datapoints[1][1] = "1548830400";
        result.setDatapoints(datapoints);
        result.setTags(new Tags());
        result.setTarget("karmalab.stats.gauges.AirBoss.chelappabo004_karmalab_net.java.nio.BufferPool.mapped.TotalCapacity30");
        results[0] = result;
        return results;
    }

    public MetricSourceResult getMetricData() {
        MetricSourceResult result = new MetricSourceResult();
        result.setDataPoint(78.0);
        result.setEpochSecond(1548830400);
        return result;
    }

    public AnomalyRequest getAnomalyRequest() {
        AnomalyRequest request = new AnomalyRequest();
        Map detectorParams = new HashMap<String, Object>();
        detectorParams.put("upperStrong", 80.0);
        detectorParams.put("upperWeak", 70.0);
        detectorParams.put("type", "RIGHT_TAILED");

        request.setDetectorParams(detectorParams);
        request.setDetectorType("constant-detector");
        request.setHash("1.3dec7f4218c57c1839147f8ca190ed55");
        return request;
    }

    public Metric getMetric() {
        Metric metric = new Metric();
        metric.setHash("1.3dec7f4218c57c1839147f8ca190ed55");
        metric.setKey("karmalab.stats.gauges.AirBoss.chelappabo004_karmalab_net.java.nio.BufferPool.mapped.TotalCapacity30");
        return metric;
    }
}
