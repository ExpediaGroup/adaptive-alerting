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
package com.expedia.adaptivealerting.anomdetect;

import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.util.MetricUtil;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static com.expedia.adaptivealerting.anomdetect.AnomalyToMetricTransformer.AA_DETECTOR_UUID;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AnomalyToMetricTransformerTest {
    private AnomalyToMetricTransformer transformerUnderTest;
    private AnomalyResult anomalyResultWithStringMetricKey;
    
    @Before
    public void setUp() {
        this.transformerUnderTest = new AnomalyToMetricTransformer();
        initTestObjects();
    }
    
    @Test
    public void testTransformWithStringMetricKey() {
        val actualMetric = transformerUnderTest.transform(anomalyResultWithStringMetricKey);
        val actualMetricDef = actualMetric.getMetricDefinition();
        val actualTags = actualMetricDef.getTags();
        val actualVTags = actualTags.getV();
        val actualKvTags = actualTags.getKv();
        val detectorUuid = anomalyResultWithStringMetricKey.getDetectorUUID().toString();
        
        assertTrue(detectorUuid.equals(actualKvTags.get(AA_DETECTOR_UUID)));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testTransformRejectsNullAnomalyResult() {
        transformerUnderTest.transform(null);
    }
    
    @Test
    public void testTransformReturnsNullIfDetectorUuidTagIsSet() {
        val kvTags = MetricUtil.defaultKvTags();
        kvTags.put(AA_DETECTOR_UUID, UUID.randomUUID().toString());
        
        val metricDef = MetricUtil.metricDefinition(kvTags, null);
        val metricData = MetricUtil.metricData(metricDef);
        val anomResult = anomalyResult(metricData);
        
        val metricResult = transformerUnderTest.transform(anomResult);
        assertNull(metricResult);
    }
    
    private void initTestObjects() {
        val metricDef = new MetricDefinition("someKey");
        val metricData = MetricUtil.metricData(metricDef);
        this.anomalyResultWithStringMetricKey = anomalyResult(metricData);
    }
    
    private AnomalyResult anomalyResult(MetricData metricData) {
        val anomResult = new AnomalyResult();
        anomResult.setDetectorUUID(UUID.randomUUID());
        anomResult.setMetricData(metricData);
        return anomResult;
    }
}
