/*
 * Copyright 2018-2019 Expedia Group, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expedia.adaptivealerting.modelservice.test;

import com.expedia.adaptivealerting.anomdetect.source.DetectorDocument;
import com.expedia.adaptivealerting.modelservice.domain.mapping.Expression;
import com.expedia.adaptivealerting.modelservice.domain.mapping.Field;
import com.expedia.adaptivealerting.modelservice.domain.mapping.Operand;
import com.expedia.adaptivealerting.modelservice.domain.mapping.Operator;
import com.expedia.adaptivealerting.modelservice.domain.mapping.User;
import com.expedia.adaptivealerting.modelservice.domain.mapping.ConsumerDetectorMapping;
import com.expedia.adaptivealerting.modelservice.domain.percolator.BoolCondition;
import com.expedia.adaptivealerting.modelservice.domain.percolator.MustCondition;
import com.expedia.adaptivealerting.modelservice.domain.percolator.PercolatorDetectorMapping;
import com.expedia.adaptivealerting.modelservice.domain.percolator.Query;
import com.expedia.adaptivealerting.modelservice.metricsource.graphite.GraphiteResult;
import com.expedia.adaptivealerting.modelservice.web.request.AnomalyRequest;
import com.expedia.adaptivealerting.modelservice.metricsource.MetricSourceResult;
import com.expedia.adaptivealerting.modelservice.util.DateUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.val;

import java.util.ArrayList;
import java.util.HashMap;
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
                .setDetectorType("constant-detector")
                .setDetectorUuid(UUID.randomUUID())
                .setDetectorParams(getDetectorParams())
                .setMetricTags("what=bookings");
    }

    public Map<String, Object> getDetectorParams() {
        // FIXME Use serialization rather than hand-crafted JSON. [WLW]
        val thresholds = "{\"thresholds\": {\"lowerStrong\": \"70\", \"lowerWeak\": \"90\"}}";
        val detectorParams = toObject(thresholds);
        detectorParams.put("type", "LEFT_TAILED");
        return detectorParams;
    }

    /**
     * Returns a detector document with the UUID set to {@literal null}.
     *
     * @return Detector document with the UUID set to {@literal null}.
     */
    public DetectorDocument getDetectorDocument() {
        val detector = new DetectorDocument();
        detector.setCreatedBy("user");
        detector.setType("constant-detector");

        Map<String, Object> detectorConfig = new HashMap<>();
        val thresholds = "{\"thresholds\": {\"lowerStrong\": \"90\", \"lowerWeak\": \"70\"}}";
        val detectorParams = toObject(thresholds);
        detectorParams.put("type", "LEFT_TAILED");
        detectorConfig.put("params", getDetectorParams());
        detector.setConfig(detectorConfig);
        return detector;
    }

    public Map<String, Object> getIllegalDetectorParams() {
        val thresholds = "{\"thresholds\": {\"lowerStrong\": \"90\", \"lowerWeak\": \"70\"}}";
        val detectorParams = toObject(thresholds);
        detectorParams.put("type", "LEFT_TAILED");
        return detectorParams;
    }

    public DetectorDocument getIllegalParamsDetector() {
        DetectorDocument detector = new DetectorDocument();
        detector.setCreatedBy("user");
        detector.setType("constant-detector");

        Map<String, Object> detectorConfig = new HashMap<>();
        val thresholds = "{\"thresholds\": {\"lowerStrong\": \"90\", \"lowerWeak\": \"70\"}}";
        val detectorParams = toObject(thresholds);
        detectorParams.put("type", "LEFT_TAILED");
        detectorConfig.put("params", getIllegalDetectorParams());
        detector.setConfig(detectorConfig);
        return detector;
    }

    public DetectorDocument getElasticsearchDetector() {
        return new DetectorDocument()
                .setUuid(UUID.fromString("aeb4d849-847a-45c0-8312-dc0fcf22b639"))
                .setCreatedBy("test-user")
                .setConfig(new HashMap<>())
                .setEnabled(true)
                .setTrusted(true)
                .setLastUpdateTimestamp(DateUtil.toUtcDate("2019-04-06 22:00:00"));
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
        percolatorDetectorMapping.setConsumerDetectorMapping(buildConsumerDetectorMapping("aeb4d849-847a-45c0-8312-dc0fcf22b639"));
        percolatorDetectorMapping.setQuery(query);
        return percolatorDetectorMapping;
    }

    public Expression getExpression() {
        Expression expression = new Expression();
        expression.setOperator(Operator.AND);
        List<Operand> operandsList = new ArrayList<>();
        Operand testOperand = new Operand();
        testOperand.setField(new Field("name", "sample-app"));
        operandsList.add(testOperand);
        expression.setOperands(operandsList);
        return expression;
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

    private ConsumerDetectorMapping buildConsumerDetectorMapping(String detectorUuid) {
        return new ConsumerDetectorMapping("cid", UUID.fromString(detectorUuid));
    }
}