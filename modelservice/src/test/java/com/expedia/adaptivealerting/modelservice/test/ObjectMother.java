package com.expedia.adaptivealerting.modelservice.test;

import com.expedia.adaptivealerting.modelservice.entity.ElasticSearchDetector;
import com.expedia.adaptivealerting.modelservice.entity.Metric;
import com.expedia.adaptivealerting.modelservice.providers.graphite.GraphiteResult;
import com.expedia.adaptivealerting.modelservice.service.AnomalyRequest;
import com.expedia.adaptivealerting.modelservice.spi.MetricSourceResult;
import com.expedia.adaptivealerting.modelservice.util.DateUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

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
        result.setDatapoints(getDataPoints());
        result.setTags(getTags());
        result.setTarget("target");
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
        request.setDetectorParams(getDetectorParams());
        request.setDetectorType("constant-detector");
        request.setMetricTags("what=bookings");
        return request;
    }

    public Map<String, Object> getDetectorParams() {
        String thresholds = "{\"thresholds\": {\"lowerStrong\": \"70\", \"lowerWeak\": \"90\"}}";
        Map<String, Object> detectorParams = toObject(thresholds);
        detectorParams.put("type", "LEFT_TAILED");
        return detectorParams;
    }

    public Metric getMetric() {
        Metric metric = new Metric();
        metric.setHash("1.3dec7f4218c57c1839147f8ca190ed55");
        metric.setKey("key");
        return metric;
    }

    public ElasticSearchDetector getElasticSearchDetector() {
        ElasticSearchDetector elasticSearchDetector = new ElasticSearchDetector();
        elasticSearchDetector.setId("1");
        elasticSearchDetector.setCreatedBy("user");
        elasticSearchDetector.setUuid("uuid");
        elasticSearchDetector.setDetectorConfig(new HashMap<>());
        elasticSearchDetector.setEnabled(true);
        elasticSearchDetector.setLastUpdateTimestamp(DateUtil.toUTCDate("2019-04-06 22:00:00"));
        return elasticSearchDetector;
    }

    @SneakyThrows
    private Map<String, Object> toObject(String message) {
        return new ObjectMapper().readValue(message, HashMap.class);
    }


    private String[][] getDataPoints() {
        String[][] datapoints = new String[2][2];
        datapoints[0][0] = String.valueOf(78.0);
        datapoints[0][1] = String.valueOf(1548829800);
        datapoints[1][0] = String.valueOf(81.0);
        datapoints[1][1] = String.valueOf(1548830400);
        return datapoints;
    }

    private Map<String, Object> getTags() {
        Map<String, Object> tags = new HashMap<String, Object>();
        tags.put("lob", "hotel");
        tags.put("pos", "expedia-com");
        return tags;
    }
}
