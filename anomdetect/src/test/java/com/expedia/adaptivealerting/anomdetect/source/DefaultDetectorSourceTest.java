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

import com.expedia.adaptivealerting.anomdetect.util.*;
import com.expedia.metrics.MetricDefinition;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@Slf4j
public final class DefaultDetectorSourceTest {
    private static final UUID DETECTOR_UUID = UUID.fromString("90c37a3c-f6bb-4c00-b41b-191909cccfb7");
    private static final UUID DETECTOR_UUID_MISSING_DETECTOR = UUID.fromString("90c37a3c-f6bb-4c00-b41b-191909cccfb8");
    private static final UUID DETECTOR_UUID_EXCEPTION = UUID.fromString("90c37a3c-f6bb-4c00-b41b-191909cccfb9");
    private static final String DETECTOR_TYPE = "ewma-detector";

    private DefaultDetectorSource sourceUnderTest;

    @Mock
    private ModelServiceConnector connector;

    private MetricDefinition metricDef;
    private MetricDefinition metricDefException;
    private DetectorResources detectorResources;
    private ModelResource modelResource;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.sourceUnderTest = new DefaultDetectorSource(connector);
        initTestObjects();
        initDependencies();
    }

    @Test
    public void testFindDetectorTypes() {
        assertNotNull(sourceUnderTest.findDetectorTypes());
    }

    @Test
    public void testFindDetectorUuids() {
        val results = sourceUnderTest.findDetectorUuids(metricDef);
        assertEquals(1, results.size());

        val result = results.get(0);
        assertEquals(DETECTOR_UUID, result);
    }

    @Test(expected = RuntimeException.class)
    public void testFindDetectorMetas_exception() {
        sourceUnderTest.findDetectorUuids(metricDefException);
    }

    @Test
    public void testFindDetector() {
        val result = sourceUnderTest.findDetector(DETECTOR_UUID, metricDef);
        assertNotNull(result);
        assertEquals(DETECTOR_UUID, result.getUuid());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindDetector_nullMeta() {
        sourceUnderTest.findDetector(null, metricDef);
    }

    @Test
    public void testFindDetector_missingDetector() {
        val result = sourceUnderTest.findDetector(DETECTOR_UUID_MISSING_DETECTOR, metricDef);
        assertNull(result);
    }

    @Test(expected = RuntimeException.class)
    public void testFindDetector_exception() {
        sourceUnderTest.findDetector(DETECTOR_UUID_EXCEPTION, metricDef);
    }

    private void initTestObjects() {
        this.metricDef = new MetricDefinition("my-metric");
        this.metricDefException = new MetricDefinition("metric-that-causes-exception");

        val detectorResource = new DetectorResource(DETECTOR_UUID.toString(), new ModelTypeResource(DETECTOR_TYPE));
        this.detectorResources = new DetectorResources(Collections.singletonList(detectorResource));

        val params = new HashMap<String, Object>();
        params.put("alpha", 0.2);
        params.put("weakSigmas", 2.0);
        params.put("strongSigmas", 4.0);

        this.modelResource = new ModelResource();
        modelResource.setUuid(DETECTOR_UUID);
        modelResource.setParams(params);
        modelResource.setDetectorType(new ModelTypeResource(DETECTOR_TYPE));
    }

    private void initDependencies() throws IOException {
        when(connector.findDetectors(metricDef)).thenReturn(detectorResources);
        when(connector.findDetectors(metricDefException))
                .thenThrow(new IOException("Error reading detectors"));

        when(connector.findLatestModel(DETECTOR_UUID)).thenReturn(modelResource);
        when(connector.findLatestModel(DETECTOR_UUID_MISSING_DETECTOR)).thenReturn(null);
        when(connector.findLatestModel(DETECTOR_UUID_EXCEPTION))
                .thenThrow(new IOException("Error reading detector"));
    }
}
