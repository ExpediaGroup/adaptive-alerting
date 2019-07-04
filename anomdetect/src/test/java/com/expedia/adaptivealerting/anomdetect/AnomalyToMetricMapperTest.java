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

import com.expedia.adaptivealerting.anomdetect.detect.AnomalyLevel;
import com.expedia.adaptivealerting.anomdetect.detect.OutlierDetectorResult;
import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;
import com.expedia.adaptivealerting.anomdetect.util.MetricUtil;
import com.expedia.metrics.MetricDefinition;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AnomalyToMetricMapperTest {
    private AnomalyToMetricMapper mapperUnderTest;
    private MappedMetricData anomalyWithStringMetricKey;

    @Before
    public void setUp() {
        this.mapperUnderTest = new AnomalyToMetricMapper();
        initTestObjects();
    }

    @Test
    public void testToMetricData_stringMetricKey() {
        val actualMetric = mapperUnderTest.toMetricData(anomalyWithStringMetricKey);
        val actualMetricDef = actualMetric.getMetricDefinition();
        val actualTags = actualMetricDef.getTags();
        val actualKvTags = actualTags.getKv();
        val detectorUuid = anomalyWithStringMetricKey.getDetectorUuid().toString();
        val outlierResult = (OutlierDetectorResult) anomalyWithStringMetricKey.getAnomalyResult();
        val outlierLevel = outlierResult.getAnomalyLevel();

        assertEquals(detectorUuid, actualKvTags.get(AnomalyToMetricMapper.AA_DETECTOR_UUID));
        assertEquals(outlierLevel.toString(), actualKvTags.get(AnomalyToMetricMapper.AA_ANOMALY_LEVEL));
    }

    @Test
    public void testToMetricDataMapsNullToNull() {
        assertNull(mapperUnderTest.toMetricData(null));
    }

    @Test
    public void testToMetricData_aaDetectorUuidToNull() {
        val detectorUuid = UUID.randomUUID();
        testToMetricData_reservedTagToNull(AnomalyToMetricMapper.AA_DETECTOR_UUID, detectorUuid.toString());
    }

    @Test
    public void testToMetricData_aaAnomalyLevelToNull() {
        val anomalyLevel = AnomalyLevel.STRONG;
        testToMetricData_reservedTagToNull(AnomalyToMetricMapper.AA_ANOMALY_LEVEL, anomalyLevel.toString());
    }

    private void initTestObjects() {
        val metricDef = new MetricDefinition("someKey");
        val metricData = MetricUtil.metricData(metricDef);
        val mmd = new MappedMetricData(metricData, UUID.randomUUID());
        mmd.setAnomalyResult(new OutlierDetectorResult(AnomalyLevel.STRONG));
        this.anomalyWithStringMetricKey = mmd;
    }

    private void testToMetricData_reservedTagToNull(String tagKey, String tagValue) {
        val kvTags = MetricUtil.defaultKvTags();
        kvTags.put(tagKey, tagValue);

        val metricDef = MetricUtil.metricDefinition(kvTags, null);
        val metricData = MetricUtil.metricData(metricDef);
        val mmd = new MappedMetricData(metricData, UUID.randomUUID());
        mmd.setAnomalyResult(new OutlierDetectorResult());

        assertNull(mapperUnderTest.toMetricData(mmd));
    }
}
