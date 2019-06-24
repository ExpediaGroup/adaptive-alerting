package com.expedia.adaptivealerting.modelservice.test;

import com.expedia.adaptivealerting.modelservice.request.MatchingDetectorsResponse;
import com.expedia.adaptivealerting.modelservice.request.User;
import com.expedia.adaptivealerting.modelservice.request.BoolCondition;
import com.expedia.adaptivealerting.modelservice.request.MustCondition;
import com.expedia.adaptivealerting.modelservice.request.PercolatorDetectorMapping;
import com.expedia.adaptivealerting.modelservice.request.Query;
import com.expedia.adaptivealerting.modelservice.entity.Detector;
import com.expedia.adaptivealerting.modelservice.entity.DetectorMapping;
import com.expedia.adaptivealerting.modelservice.plugin.graphite.GraphiteResult;
import com.expedia.adaptivealerting.modelservice.request.AnomalyRequest;
import com.expedia.adaptivealerting.modelservice.plugin.MetricSourceResult;
import com.expedia.adaptivealerting.modelservice.util.DateUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.val;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        return new AnomalyRequest()
                .setDetectorParams(getDetectorParams())
                // FIXME This is a legacy detector type
                .setDetectorType("constant-detector")
                .setMetricTags("what=bookings");
    }

    public Map<String, Object> getDetectorParams() {
        val thresholds = "{\"thresholds\": {\"lowerStrong\": \"70\", \"lowerWeak\": \"90\"}}";
        val detectorParams = toObject(thresholds);
        detectorParams.put("type", "LEFT_TAILED");
        return detectorParams;
    }

    public Detector getElasticsearchDetector() {
        return new Detector()
                .setUuid("aeb4d849-847a-45c0-8312-dc0fcf22b639")
                .setCreatedBy("test-user")
                .setDetectorConfig(new HashMap<>())
                .setEnabled(true)
                .setLastUpdateTimestamp(DateUtil.toUtcDate("2019-04-06 22:00:00"));
    }

    public DetectorMapping getDetectorMapping() {
        DetectorMapping detectorMapping = new DetectorMapping();
        detectorMapping.setCreatedTimeInMillis(10000);
        detectorMapping.setEnabled(true);
        detectorMapping.setUser(new User("test-user"));
        detectorMapping.setDetector(new com.expedia.adaptivealerting.modelservice.request.Detector(UUID.fromString("aeb4d849-847a-45c0-8312-dc0fcf22b639")));
        return detectorMapping;
    }

    public PercolatorDetectorMapping getPercolatorDetectorMapping() {
        PercolatorDetectorMapping percolatorDetectorMapping = new PercolatorDetectorMapping();
        Query query = new Query();
        BoolCondition boolCondition = new BoolCondition();
        List<MustCondition> mustConditions = new ArrayList<>();
        MustCondition mustCondition = new MustCondition();
        Map match = new HashMap<>();
        match.put("name", "sample-web");
        mustCondition.setMatch(match);
        mustConditions.add(mustCondition);
        boolCondition.setMust(mustConditions);
        query.setBool(boolCondition);
        percolatorDetectorMapping.setCreatedTimeInMillis(1554828886);
        percolatorDetectorMapping.setEnabled(true);
        percolatorDetectorMapping.setLastModifiedTimeInMillis(1554828886);
        percolatorDetectorMapping.setUser(new User("test-user"));
        percolatorDetectorMapping.setDetector(new com.expedia.adaptivealerting.modelservice.request.Detector(UUID.fromString("aeb4d849-847a-45c0-8312-dc0fcf22b639")));
        percolatorDetectorMapping.setQuery(query);
        return percolatorDetectorMapping;
    }

    public MatchingDetectorsResponse getMatchingDetectorsResponse() {
        Map<Integer, List<com.expedia.adaptivealerting.modelservice.request.Detector>> groupedDetectorsByIndex = new HashMap<>();
        com.expedia.adaptivealerting.modelservice.request.Detector detector = new com.expedia.adaptivealerting.modelservice.request.Detector(UUID.fromString("aeb4d849-847a-45c0-8312-dc0fcf22b639"));
        List<com.expedia.adaptivealerting.modelservice.request.Detector> detectors = new ArrayList<>();
        detectors.add(detector);
        groupedDetectorsByIndex.put(0, detectors);
        return new MatchingDetectorsResponse(groupedDetectorsByIndex, 10000);
    }

    public Map getTestObject() {
        Map object = new LinkedHashMap<>();
        object.put("test1", 1);
        object.put("test2", 2);
        object.put("test3", 3);
        return object;
    }

    public String getTestString() {
        return "{\"test1\":1,\"test2\":2,\"test3\":3}";
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
