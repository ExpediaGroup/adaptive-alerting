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

import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.adaptivealerting.core.util.MetricUtil;
import com.expedia.metrics.MetricDefinition;
import lombok.val;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.UUID;

import static com.expedia.adaptivealerting.anomdetect.AnomalyToMetricMapper.AA_ANOMALY_LEVEL;
import static com.expedia.adaptivealerting.anomdetect.AnomalyToMetricMapper.AA_DETECTOR_UUID;
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

    // FIXME Remove @Ignore after completing my diagnostic test. [WLW]
    @Test
    @Ignore
    public void testToMetricData_stringMetricKey() {
        val actualMetric = mapperUnderTest.toMetricData(anomalyWithStringMetricKey);
        val actualMetricDef = actualMetric.getMetricDefinition();
        val actualTags = actualMetricDef.getTags();
        val actualKvTags = actualTags.getKv();
        val detectorUuid = anomalyWithStringMetricKey.getDetectorUuid().toString();
        val anomalyLevel = anomalyWithStringMetricKey.getAnomalyResult().getAnomalyLevel().toString();

        assertEquals(detectorUuid, actualKvTags.get(AA_DETECTOR_UUID));
        assertEquals(anomalyLevel, actualKvTags.get(AA_ANOMALY_LEVEL));
    }

    @Test
    public void testToMetricDataMapsNullToNull() {
        assertNull(mapperUnderTest.toMetricData(null));
    }

    @Test
    public void testToMetricData_aaDetectorUuidToNull() {
        val detectorUuid = UUID.randomUUID();
        testToMetricData_reservedTagToNull(AA_DETECTOR_UUID, detectorUuid.toString());
    }

    @Test
    public void testToMetricData_aaAnomalyLevelToNull() {
        val anomalyLevel = AnomalyLevel.STRONG;
        testToMetricData_reservedTagToNull(AA_ANOMALY_LEVEL, anomalyLevel.toString());
    }

    private void initTestObjects() {
        val metricDef = new MetricDefinition("someKey");
        val metricData = MetricUtil.metricData(metricDef);
        val mmd = new MappedMetricData(metricData, UUID.randomUUID());
        mmd.setAnomalyResult(new AnomalyResult(AnomalyLevel.STRONG));
        this.anomalyWithStringMetricKey = mmd;
    }

    private void testToMetricData_reservedTagToNull(String tagKey, String tagValue) {
        val kvTags = MetricUtil.defaultKvTags();
        kvTags.put(tagKey, tagValue);

        val metricDef = MetricUtil.metricDefinition(kvTags, null);
        val metricData = MetricUtil.metricData(metricDef);
        val mmd = new MappedMetricData(metricData, UUID.randomUUID());
        mmd.setAnomalyResult(new AnomalyResult());

        assertNull(mapperUnderTest.toMetricData(mmd));
    }
}
