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

import com.expedia.adaptivealerting.anomdetect.util.DetectorMeta;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.TagCollection;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.when;

@Slf4j
public final class TempHaystackAwareDetectorSourceTest {
    private TempHaystackAwareDetectorSource source;
    
    @Mock
    private DetectorSource primaryDetectorSource;
    
    private MetricDefinition haystackMetricDef;
    private MetricDefinition nonHaystackMetricDef;
    private DetectorMeta nonHaystackDetectorMeta;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
        this.source = new TempHaystackAwareDetectorSource(primaryDetectorSource);
    }
    
    @Test
    public void testFindDetectorUUIDs_sameMetricHasSameUUID() {
        val results1 = source.findDetectorMetas(haystackMetricDef);
        val results2 = source.findDetectorMetas(haystackMetricDef);
    
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
        val haystackResults = source.findDetectorMetas(haystackMetricDef);
        val nonHaystackResults = source.findDetectorMetas(nonHaystackMetricDef);
        
        assertEquals(1, haystackResults.size());
        assertEquals(1, nonHaystackResults.size());
    
        val uuid1 = haystackResults.get(0);
        val uuid2 = nonHaystackResults.get(0);
        
        log.info("Different metrics have different UUIDs:");
        log.info("  uuid1={}", uuid1);
        log.info("  uuid2={}", uuid2);
        
        assertNotEquals(uuid2, uuid1);
    }
    
    @Test
    public void testFindDetector() {
        val detectorUuid = UUID.randomUUID();
        val detector = source.findDetector(detectorUuid, haystackMetricDef);
        
        assertEquals(detectorUuid, detector.getUuid());
    }
    
    private void initTestObjects() {
        this.haystackMetricDef = new MetricDefinition(
                "haystack-metric",
                new TagCollection(Collections.singletonMap("product", "haystack")),
                TagCollection.EMPTY);
        
        this.nonHaystackMetricDef = new MetricDefinition("non-haystack-metric");
        
        this.nonHaystackDetectorMeta = new DetectorMeta(UUID.randomUUID(), "constant-detector");
    }
    
    private void initDependencies() {
        when(primaryDetectorSource.findDetectorMetas(haystackMetricDef))
                .thenReturn(Collections.EMPTY_LIST);
        when(primaryDetectorSource.findDetectorMetas(nonHaystackMetricDef))
                .thenReturn(Collections.singletonList(nonHaystackDetectorMeta));
    }
}
