/*
 * Copyright 2018 Expedia Group, Inc.
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

import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.adaptivealerting.core.metrics.MetricData;
import com.expedia.adaptivealerting.core.metrics.MetricDefinition;
import com.expedia.adaptivealerting.core.metrics.TagCollection;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Set;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * @author Willie Wheeler
 */
public final class AnomalyDetectorMapperTest {

    // Class under test
    private AnomalyDetectorMapper mapper;

    // Test objects
    private MetricData mpointWithDetectors;
    private MetricData mpointWithoutDetectors;

    @Before
    public void setUp() {
        this.mapper = new AnomalyDetectorMapper();

        // TODO For now, this is known to have detectors.
        // We'll need to update this once we un-hardcode the AnomalyDetectorMapper.
        final MetricDefinition metricWithDetectors = new MetricDefinition(new TagCollection(
                new HashMap<String, String>() {{
                    put("unit", "dummy");
                    put("mtype", "dummy");
                    put("what", "bookings");
                }}));
        this.mpointWithDetectors = new MetricData(metricWithDetectors, 9, System.currentTimeMillis());

        // TODO For now, this is known to have no detectors. See above.
        final MetricDefinition metricWithoutDetectors = new MetricDefinition(new TagCollection(
                new HashMap<String, String>() {{
                    put("unit", "dummy");
                    put("mtype", "dummy");
                }}));
        this.mpointWithoutDetectors = new MetricData(metricWithoutDetectors, 9, System.currentTimeMillis());
    }

    @Test
    @Ignore
    public void testMap_mpointWithDetectors() {
        final Set<MappedMetricData> results = mapper.map(mpointWithDetectors);
        assertFalse(results.isEmpty());
    }

    @Test
    @Ignore
    public void testMap_mpointWithoutDetectors() {
        final Set<MappedMetricData> results = mapper.map(mpointWithoutDetectors);
        assertTrue(results.isEmpty());
    }
}
