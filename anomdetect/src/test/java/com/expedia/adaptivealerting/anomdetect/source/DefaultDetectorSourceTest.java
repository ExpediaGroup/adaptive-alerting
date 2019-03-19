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

import com.expedia.adaptivealerting.anomdetect.util.DetectorNotFoundException;
import com.expedia.adaptivealerting.anomdetect.util.DetectorResource;
import com.expedia.adaptivealerting.anomdetect.util.DetectorResources;
import com.expedia.adaptivealerting.anomdetect.util.DetectorRetrievalException;
import com.expedia.adaptivealerting.anomdetect.util.ModelResource;
import com.expedia.adaptivealerting.anomdetect.util.ModelServiceConnector;
import com.expedia.adaptivealerting.anomdetect.util.ModelTypeResource;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
    private DetectorResources updatedDetectorResources;
    private ModelResource modelResource;

    @Before
    public void setUp() {
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

    @Test
    public void testFindUpdatedDetectorUuids() {
        val results = sourceUnderTest.findUpdatedDetectors(1);
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

    @Test(expected = DetectorNotFoundException.class)
    public void testFindDetector_missingDetector() {
        sourceUnderTest.findDetector(DETECTOR_UUID_MISSING_DETECTOR, metricDef);
    }

    @Test(expected = RuntimeException.class)
    public void testFindDetector_exception() {
        sourceUnderTest.findDetector(DETECTOR_UUID_EXCEPTION, metricDef);
    }

    private void initTestObjects() {
        this.metricDef = new MetricDefinition("my-metric");
        this.metricDefException = new MetricDefinition("metric-that-causes-exception");

        val detectorResource = new DetectorResource(DETECTOR_UUID.toString(), new ModelTypeResource(DETECTOR_TYPE),true);
        this.detectorResources = new DetectorResources(Collections.singletonList(detectorResource));


        val updatedDetectorsResource = new DetectorResource(DETECTOR_UUID.toString(), new ModelTypeResource(DETECTOR_TYPE),true);
        this.updatedDetectorResources = new DetectorResources(Collections.singletonList(updatedDetectorsResource));

        val params = new HashMap<String, Object>();
        params.put("alpha", 0.2);
        params.put("weakSigmas", 2.0);
        params.put("strongSigmas", 4.0);

        this.modelResource = new ModelResource();
        modelResource.setUuid(DETECTOR_UUID);
        modelResource.setParams(params);
        modelResource.setDetectorType(new ModelTypeResource(DETECTOR_TYPE));
    }

    private void initDependencies() {
        when(connector.findDetectors(metricDef)).thenReturn(detectorResources);
        when(connector.findDetectors(metricDefException))
                .thenThrow(new DetectorRetrievalException("Error finding detectors", new IOException()));

        when(connector.findLatestModel(DETECTOR_UUID)).thenReturn(modelResource);
        when(connector.findLatestModel(DETECTOR_UUID_MISSING_DETECTOR))
                .thenThrow(new DetectorNotFoundException("No models found"));
        when(connector.findLatestModel(DETECTOR_UUID_EXCEPTION))
                .thenThrow(new DetectorRetrievalException("Error finding latest model", new IOException()));
        when(connector.findUpdatedDetectors(1)).thenReturn(updatedDetectorResources);
    }
}
