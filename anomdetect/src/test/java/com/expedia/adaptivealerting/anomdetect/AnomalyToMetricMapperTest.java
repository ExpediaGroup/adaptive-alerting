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
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.adaptivealerting.core.util.MetricUtil;
import com.expedia.metrics.MetricDefinition;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static com.expedia.adaptivealerting.anomdetect.AnomalyToMetricMapper.AA_DETECTOR_UUID;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AnomalyToMetricMapperTest {
    private static final String DETECTOR_TYPE = "cusum-detector";

    private AnomalyToMetricMapper mapperUnderTest;
    private MappedMetricData anomalyWithStringMetricKey;
    
    @Before
    public void setUp() {
        this.mapperUnderTest = new AnomalyToMetricMapper();
        initTestObjects();
    }
    
    @Test
    public void toMetricDataWithStringMetricKey() {
        val actualMetric = mapperUnderTest.toMetricData(anomalyWithStringMetricKey);
        val actualMetricDef = actualMetric.getMetricDefinition();
        val actualTags = actualMetricDef.getTags();
        val actualKvTags = actualTags.getKv();
        val detectorUuid = anomalyWithStringMetricKey.getDetectorUuid().toString();
        
        assertTrue(detectorUuid.equals(actualKvTags.get(AA_DETECTOR_UUID)));
    }
    
    @Test
    public void toMetricDataMapsNullToNull() {
        assertNull(mapperUnderTest.toMetricData(null));
    }
    
    @Test
    public void toMetricDataMapsAnomalyHavingAADetectorUuidTagToNull() {
        val detectorUuid = UUID.randomUUID();

        val kvTags = MetricUtil.defaultKvTags();
        kvTags.put(AA_DETECTOR_UUID, detectorUuid.toString());
        
        val metricDef = MetricUtil.metricDefinition(kvTags, null);
        val metricData = MetricUtil.metricData(metricDef);
        val mmd = new MappedMetricData(metricData, detectorUuid, DETECTOR_TYPE);
        mmd.setAnomalyResult(new AnomalyResult());

        assertNull(mapperUnderTest.toMetricData(mmd));
    }

    private void initTestObjects() {
        val metricDef = new MetricDefinition("someKey");
        val metricData = MetricUtil.metricData(metricDef);
        val mmd = new MappedMetricData(metricData, UUID.randomUUID(), DETECTOR_TYPE);
        this.anomalyWithStringMetricKey = mmd;
    }
}
