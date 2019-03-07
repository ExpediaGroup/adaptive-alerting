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

import com.expedia.adaptivealerting.anomdetect.AnomalyDetector;
import com.expedia.adaptivealerting.anomdetect.constant.ConstantThresholdAnomalyDetector;
import com.expedia.adaptivealerting.anomdetect.ewma.EwmaAnomalyDetector;
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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@Slf4j
public final class TempHaystackAwareDetectorSourceTest {
    private TempHaystackAwareDetectorSource sourceUnderTest;
    
    @Mock
    private DetectorSource primaryDetectorSource;

    private Set<String> detectorTypes;

    private MetricDefinition haystackMetricDef;
    private MetricDefinition nonHaystackMetricDef;

    private DetectorMeta persistentHaystackDetectorMeta;
    private DetectorMeta dynamicHaystackDetectorMeta;
    private DetectorMeta nonHaystackDetectorMeta;
    private DetectorMeta detectorMetaMissingDetector;

    private AnomalyDetector haystackDetector;
    private AnomalyDetector nonHaystackDetector;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
        this.sourceUnderTest = new TempHaystackAwareDetectorSource(primaryDetectorSource);
    }

    @Test
    public void testFindDetectorTypes() {
        val typeResults = sourceUnderTest.findDetectorTypes();
        assertSame(detectorTypes, typeResults);
    }

    @Test
    public void testFindDetectorUUIDs_sameMetricHasSameUUID() {
        val results1 = sourceUnderTest.findDetectorMetas(haystackMetricDef);
        val results2 = sourceUnderTest.findDetectorMetas(haystackMetricDef);
    
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
        val haystackResults = sourceUnderTest.findDetectorMetas(haystackMetricDef);
        val nonHaystackResults = sourceUnderTest.findDetectorMetas(nonHaystackMetricDef);
        
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
    public void testFindDetector_persistentHaystackDetector() {
        val result = sourceUnderTest.findDetector(persistentHaystackDetectorMeta, haystackMetricDef);
        assertSame(haystackDetector, result);
    }

    @Test
    public void testFindDetector_dynamicHaystackDetector() {
        val result = sourceUnderTest.findDetector(dynamicHaystackDetectorMeta, haystackMetricDef);
        assertNotNull(result);
    }

    @Test
    public void testFindDetector_nonHaystackDetector() {
        val result = sourceUnderTest.findDetector(nonHaystackDetectorMeta, nonHaystackMetricDef);
        assertSame(nonHaystackDetector, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindDetector_nullDetectorMeta() {
        sourceUnderTest.findDetector(null, nonHaystackMetricDef);
    }

    @Test
    public void testFindDetector_nullMetricDef() {
        // Normally we don't need a MetricDefinition to find a detector at all--the detectorMeta
        // is enough since it contains the detector UUID. But with the TempHaystackAwareDetectorSource
        // we want to be able to check the MetricDefinition to see whether it's a Haystack metric,
        // because in that case we currently create a detector dynamically. [WLW]
        val result = sourceUnderTest.findDetector(nonHaystackDetectorMeta, null);
        assertNull(result);
    }

    @Test
    public void testFindDetector_missingDetector() {
        val result = sourceUnderTest.findDetector(detectorMetaMissingDetector, nonHaystackMetricDef);
        assertNull(result);
    }
    
    private void initTestObjects() {
        this.detectorTypes = new HashSet<>();

        this.haystackMetricDef = new MetricDefinition(
                "haystack-metric",
                new TagCollection(Collections.singletonMap("product", "haystack")),
                TagCollection.EMPTY);
        
        this.nonHaystackMetricDef = new MetricDefinition("non-haystack-metric");

        this.persistentHaystackDetectorMeta = new DetectorMeta(UUID.randomUUID(), "ewma-haystackDetector");
        this.dynamicHaystackDetectorMeta = new DetectorMeta(UUID.randomUUID(), "ewma-haystackDetector");
        this.nonHaystackDetectorMeta = new DetectorMeta(UUID.randomUUID(), "constant-haystackDetector");
        this.detectorMetaMissingDetector = new DetectorMeta(UUID.randomUUID(), "ewma-missingDetector");

        this.haystackDetector = new EwmaAnomalyDetector();
        this.nonHaystackDetector = new ConstantThresholdAnomalyDetector();
    }
    
    private void initDependencies() {
        when(primaryDetectorSource.findDetectorTypes())
                .thenReturn(detectorTypes);

        when(primaryDetectorSource.findDetectorMetas(haystackMetricDef))
                .thenReturn(Collections.EMPTY_LIST);
        when(primaryDetectorSource.findDetectorMetas(nonHaystackMetricDef))
                .thenReturn(Collections.singletonList(nonHaystackDetectorMeta));

        when(primaryDetectorSource.findDetector(persistentHaystackDetectorMeta, haystackMetricDef))
                .thenReturn(haystackDetector);
        when(primaryDetectorSource.findDetector(dynamicHaystackDetectorMeta, haystackMetricDef))
                .thenReturn(null);
        when(primaryDetectorSource.findDetector(nonHaystackDetectorMeta, nonHaystackMetricDef))
                .thenReturn(nonHaystackDetector);
        when(primaryDetectorSource.findDetector(detectorMetaMissingDetector, nonHaystackMetricDef))
                .thenReturn(null);
    }
}
