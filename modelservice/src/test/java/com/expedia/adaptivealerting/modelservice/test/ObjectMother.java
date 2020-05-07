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
import com.expedia.adaptivealerting.modelservice.entity.Detector;
import com.expedia.adaptivealerting.modelservice.model.mapping.Expression;
import com.expedia.adaptivealerting.modelservice.model.mapping.Field;
import com.expedia.adaptivealerting.modelservice.model.mapping.Operand;
import com.expedia.adaptivealerting.modelservice.model.mapping.Operator;
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

    /**
     * Returns a detector document with the UUID set to {@literal null}.
     *
     * @return Detector document with the UUID set to {@literal null}.
     */
    public DetectorDocument buildDetectorDocument() {
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

    /**
     * Returns a detector entity with the UUID set to {@literal null}.
     *
     * @return Detector with the UUID set to {@literal null}.
     */
    public Detector buildDetector() {
        Detector detector = new Detector();
        detector.setType("constant-detector");
        Map<String, Object> detectorConfig = new HashMap<>();
        val thresholds = "{\"thresholds\": {\"lowerStrong\": \"90\", \"lowerWeak\": \"70\"}}";
        val detectorParams = toObject(thresholds);
        detectorParams.put("type", "LEFT_TAILED");
        detectorConfig.put("params", getDetectorParams());
        detector.setDetectorConfig(detectorConfig);
        return detector;
    }

    private Map<String, Object> getDetectorParams() {
        // FIXME Use serialization rather than hand-crafted JSON. [WLW]
        val thresholds = "{\"thresholds\": {\"lowerStrong\": \"70\", \"lowerWeak\": \"90\"}}";
        val detectorParams = toObject(thresholds);
        detectorParams.put("type", "LEFT_TAILED");
        return detectorParams;
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
}

