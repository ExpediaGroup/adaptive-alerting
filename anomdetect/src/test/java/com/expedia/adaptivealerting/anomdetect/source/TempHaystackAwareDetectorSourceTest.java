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
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.TagCollection;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

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

    private UUID persistentHaystackDetectorUuid;
    private UUID dynamicHaystackDetectorUuid;
    private UUID nonHaystackDetectorUuid;
    private UUID missingDetectorUuid;

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
        val results1 = sourceUnderTest.findDetectorUuids(haystackMetricDef);
        val results2 = sourceUnderTest.findDetectorUuids(haystackMetricDef);

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
        val haystackResults = sourceUnderTest.findDetectorUuids(haystackMetricDef);
        val nonHaystackResults = sourceUnderTest.findDetectorUuids(nonHaystackMetricDef);

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
        val result = sourceUnderTest.findDetector(persistentHaystackDetectorUuid, haystackMetricDef);
        assertSame(haystackDetector, result);
    }

    @Test
    public void testFindDetector_dynamicHaystackDetector() {
        val result = sourceUnderTest.findDetector(dynamicHaystackDetectorUuid, haystackMetricDef);
        assertNotNull(result);
    }

    @Test
    public void testFindDetector_nonHaystackDetector() {
        val result = sourceUnderTest.findDetector(nonHaystackDetectorUuid, nonHaystackMetricDef);
        assertSame(nonHaystackDetector, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindDetector_nullDetectorMeta() {
        sourceUnderTest.findDetector(null, nonHaystackMetricDef);
    }

    @Test
    public void testFindDetector_nullMetricDef() {
        // Normally we don't need a MetricDefinition to find a detector at all--the detector UUID
        // is enough. But with the TempHaystackAwareDetectorSource
        // we want to be able to check the MetricDefinition to see whether it's a Haystack metric,
        // because in that case we currently create a detector dynamically. [WLW]
        val result = sourceUnderTest.findDetector(nonHaystackDetectorUuid, null);
        assertNull(result);
    }

    @Test
    public void testFindDetector_missingDetector() {
        val result = sourceUnderTest.findDetector(missingDetectorUuid, nonHaystackMetricDef);
        assertNull(result);
    }

    @Test
    public void testFindUpdatedDetectorUuids() {
        val results = sourceUnderTest.findUpdatedDetectors(1);
        assertEquals(1, results.size());
    }

    private void initTestObjects() {
        this.detectorTypes = new HashSet<>();

        this.haystackMetricDef = new MetricDefinition(
                "haystack-metric",
                new TagCollection(Collections.singletonMap("product", "haystack")),
                TagCollection.EMPTY);

        this.nonHaystackMetricDef = new MetricDefinition("non-haystack-metric");

        this.persistentHaystackDetectorUuid = UUID.randomUUID();
        this.dynamicHaystackDetectorUuid = UUID.randomUUID();
        this.nonHaystackDetectorUuid = UUID.randomUUID();
        this.missingDetectorUuid = UUID.randomUUID();

        this.haystackDetector = new EwmaAnomalyDetector();
        this.nonHaystackDetector = new ConstantThresholdAnomalyDetector();
    }

    private void initDependencies() {
        when(primaryDetectorSource.findDetectorTypes())
                .thenReturn(detectorTypes);

        when(primaryDetectorSource.findDetectorUuids(haystackMetricDef))
                .thenReturn(Collections.EMPTY_LIST);
        when(primaryDetectorSource.findDetectorUuids(nonHaystackMetricDef))
                .thenReturn(Collections.singletonList(nonHaystackDetectorUuid));

        when(primaryDetectorSource.findDetector(persistentHaystackDetectorUuid, haystackMetricDef))
                .thenReturn(haystackDetector);
        when(primaryDetectorSource.findDetector(dynamicHaystackDetectorUuid, haystackMetricDef))
                .thenReturn(null);
        when(primaryDetectorSource.findDetector(nonHaystackDetectorUuid, nonHaystackMetricDef))
                .thenReturn(nonHaystackDetector);
        when(primaryDetectorSource.findDetector(missingDetectorUuid, nonHaystackMetricDef))
                .thenReturn(null);
        when(primaryDetectorSource.findUpdatedDetectors(1))
                .thenReturn(Collections.singletonList(UUID.randomUUID()));
    }
}
