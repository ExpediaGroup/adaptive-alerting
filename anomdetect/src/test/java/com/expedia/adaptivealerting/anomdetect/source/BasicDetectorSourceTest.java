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
package com.expedia.adaptivealerting.anomdetect.source;

import com.expedia.metrics.MetricDefinition;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@Slf4j
public final class BasicDetectorSourceTest {
    private BasicDetectorSource source;
    private MetricDefinition metricDef1;
    private MetricDefinition metricDef2;
    
    @Before
    public void setUp() {
        this.source = new BasicDetectorSource();
        this.metricDef1 = new MetricDefinition("some-metric-1");
        this.metricDef2 = new MetricDefinition("some-metric-2");
    }
    
    @Test
    public void testFindDetectorUUIDs_sameMetricHasSameUUID() {
        val results1 = source.findDetectorUUIDs(metricDef1);
        val results2 = source.findDetectorUUIDs(metricDef1);
    
        assertEquals(1, results1.size());
        assertEquals(1, results2.size());
    
        val uuid1 = results1.get(0);
        val uuid2 = results2.get(0);
        
        log.info("Same metric has same UUID:");
        log.info("  uuid1={}", uuid1);
        log.info("  uuid2={}", uuid2);
    
        assertEquals(uuid2, uuid1);
    }
    
    @Test
    public void testFindDetectorUUIDs_differentMetricsHaveDifferentUUIDs() {
        val results1 = source.findDetectorUUIDs(metricDef1);
        val results2 = source.findDetectorUUIDs(metricDef2);
        
        assertEquals(1, results1.size());
        assertEquals(1, results2.size());
    
        val uuid1 = results1.get(0);
        val uuid2 = results2.get(0);
        
        log.info("Different metrics have different UUIDs:");
        log.info("  uuid1={}", uuid1);
        log.info("  uuid2={}", uuid2);
        
        assertNotEquals(uuid2, uuid1);
    }
    
    @Test
    public void testFindDetector() {
        val uuid = UUID.randomUUID();
        val detector = source.findDetector(uuid);
        
        assertEquals(uuid, detector.getUuid());
    }
}
