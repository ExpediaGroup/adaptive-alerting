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
package com.expedia.adaptivealerting.anomdetect.util;

import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.TagCollection;
import lombok.val;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public final class MetricUtilTest {

    @Test
    public void testMetricDefinition_nullTags() {
        val metricDef = MetricUtil.metricDefinition(null, null);
        val tagCollection = metricDef.getTags();
        assertNotNull(tagCollection.getKv());
        assertNotNull(tagCollection.getV());
    }

    @Test
    public void testDefaultKvTags() {
        val tags = MetricUtil.defaultKvTags();

        // Metrics 2.0 defaults
        assertEquals("", tags.get(MetricDefinition.UNIT));
        assertEquals("gauge", tags.get(MetricDefinition.MTYPE));
    }

    @Test
    public void testDefaultVTags() {
        assertTrue(MetricUtil.defaultVTags().isEmpty());
    }

    @Test
    public void testMetricData() {
        val kvTags = MetricUtil.defaultKvTags();
        val vTags = MetricUtil.defaultVTags();
        val metricDef = MetricUtil.metricDefinition(kvTags, vTags);
        val metricData = MetricUtil.metricData(metricDef);
        assertEquals(0.0, metricData.getValue(), 0.001);
    }

    @Test
    public void testMetricData_value() {
        val kvTags = MetricUtil.defaultKvTags();
        val vTags = MetricUtil.defaultVTags();
        val metricDef = MetricUtil.metricDefinition(kvTags, vTags);
        val metricData = MetricUtil.metricData(metricDef, 3.14159);
        assertEquals(3.14159, metricData.getValue(), 0.001);
    }

    @Test
    public void testMetricData_value_and_epochSecond() {
        val kvTags = MetricUtil.defaultKvTags();
        val vTags = MetricUtil.defaultVTags();
        val metricDef = MetricUtil.metricDefinition(kvTags, vTags);
        val metricData = MetricUtil.metricData(metricDef, 3.14159, 1554702637);
        assertEquals(3.14159, metricData.getValue(), 0.001);
        assertEquals(1554702637, metricData.getTimestamp(), 0.001);
    }

    @Test
    public void testGetMetricFunctionOrKey_getFunction() {
        val mappedUuid = UUID.randomUUID();
        Map<String, String> tags = new HashMap<>();
        tags.put("function", "sum(test.metrics)");
        val metricDefinition = new MetricDefinition(new TagCollection(tags));
        val metricData = new MetricData(metricDefinition, 100.0, Instant.now().getEpochSecond());
        val mappedMetricData = new MappedMetricData(metricData, mappedUuid);
        val value = MetricUtil.getMetricFunctionOrKey(mappedMetricData);
        assertEquals("sum(test.metrics)", value);
    }

    @Test
    public void testGetMetricFunctionOrKey_getKey() {
        val mappedUuid = UUID.randomUUID();
        val metricDefinition = new MetricDefinition("metric-definition");
        val metricData = new MetricData(metricDefinition, 100.0, Instant.now().getEpochSecond());
        val mappedMetricData = new MappedMetricData(metricData, mappedUuid);
        val value = MetricUtil.getMetricFunctionOrKey(mappedMetricData);
        assertEquals("metric-definition", value);
    }
}
