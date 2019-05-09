package com.expedia.adaptivealerting.modelservice.test;

import com.expedia.adaptivealerting.modelservice.entity.ElasticsearchDetector;
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

    public GraphiteResult[] getGraphiteData() {
        GraphiteResult[] results = new GraphiteResult[1];
        GraphiteResult result = new GraphiteResult();
        result.setDatapoints(getDataPoints());
        result.setTags(getTags());
        result.setTarget("target");
        results[0] = result;
        return results;
    }

    public ElasticsearchDetector getElasticsearchDetector() {
        ElasticsearchDetector elasticSearchDetector = ElasticsearchDetector.builder()
                .id("1")
                .createdBy("user")
                .uuid("uuid")
                .detectorConfig(new HashMap<>())
                .enabled(true).build();
        elasticSearchDetector.setLastUpdateTimestamp(DateUtil.toUTCDate("2019-04-06 22:00:00"));
        return elasticSearchDetector;
    }

    public Map<String, Object> getElasticSearchSource() {
        Map<String, Object> source = new HashMap<>();
        source.put("lastUpdateTimestamp", "2019-10-05 12:00:00");
        source.put("createdBy", "kashah");
        source.put("uuid", "uuid");
        source.put("enabled", true);
        source.put("detectorConfig", new HashMap<>());
        return source;
    }

    public MetricSourceResult getMetricData() {
        MetricSourceResult result = new MetricSourceResult();
        result.setDataPoint(78.0);
        result.setEpochSecond(1548830400);
        return result;
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
