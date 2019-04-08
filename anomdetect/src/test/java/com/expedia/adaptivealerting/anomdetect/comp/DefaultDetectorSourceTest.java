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
package com.expedia.adaptivealerting.anomdetect.comp;

import com.expedia.adaptivealerting.anomdetect.DetectorNotFoundException;
import com.expedia.adaptivealerting.anomdetect.DetectorRetrievalException;
import com.expedia.adaptivealerting.anomdetect.comp.connector.DetectorResource;
import com.expedia.adaptivealerting.anomdetect.comp.connector.DetectorResources;
import com.expedia.adaptivealerting.anomdetect.comp.connector.ModelResource;
import com.expedia.adaptivealerting.anomdetect.comp.connector.ModelServiceConnector;
import com.expedia.adaptivealerting.anomdetect.comp.connector.ModelTypeResource;
import com.expedia.adaptivealerting.anomdetect.comp.legacy.LegacyDetectorFactory;
import com.expedia.adaptivealerting.anomdetect.detector.Detector;
import com.expedia.adaptivealerting.anomdetect.forecast.ForecastingDetector;
import com.expedia.adaptivealerting.anomdetect.forecast.interval.ExponentialWelfordIntervalForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.point.EwmaPointForecaster;
import com.expedia.adaptivealerting.core.anomaly.AnomalyType;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Slf4j
public final class DefaultDetectorSourceTest {
    private static final String DETECTOR_TYPE_EWMA = "ewma-detector";

    private static final UUID DETECTOR_UUID_EWMA = UUID.randomUUID();
    private static final UUID DETECTOR_UUID_MISSING_DETECTOR = UUID.randomUUID();
    private static final UUID DETECTOR_UUID_EXCEPTION = UUID.randomUUID();

    private DefaultDetectorSource sourceUnderTest;

    @Mock
    private ModelServiceConnector connector;

    @Mock
    private LegacyDetectorFactory legacyDetectorFactory;

    private MetricDefinition metricDef;
    private MetricDefinition metricDefException;
    private DetectorResources detectorResources;
    private DetectorResources updatedDetectorResources;
    private ModelResource modelResource_ewma;
    private Detector detector;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
        this.sourceUnderTest = new DefaultDetectorSource(connector, legacyDetectorFactory);
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
        assertEquals(DETECTOR_UUID_EWMA, result);
    }

    @Test(expected = RuntimeException.class)
    public void testFindDetectorUuids_exception() {
        sourceUnderTest.findDetectorUuids(metricDefException);
    }

    @Test
    public void testFindUpdatedDetectorUuids() {
        val results = sourceUnderTest.findUpdatedDetectors(1);
        assertEquals(1, results.size());

        val result = results.get(0);
        assertEquals(DETECTOR_UUID_EWMA, result);
    }

    @Test
    public void testFindDetector_ewma() {
        val result = sourceUnderTest.findDetector(DETECTOR_UUID_EWMA);
        assertNotNull(result);
        assertEquals(DETECTOR_UUID_EWMA, result.getUuid());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindDetector_nullMeta() {
        sourceUnderTest.findDetector(null);
    }

    @Test(expected = DetectorNotFoundException.class)
    public void testFindDetector_missingDetector() {
        sourceUnderTest.findDetector(DETECTOR_UUID_MISSING_DETECTOR);
    }

    @Test(expected = RuntimeException.class)
    public void testFindDetector_exception() {
        sourceUnderTest.findDetector(DETECTOR_UUID_EXCEPTION);
    }

    private void initTestObjects() {
        initTestObjects_findDetectors();
        initTestObjects_findLatestModel();
    }

    private void initTestObjects_findDetectors() {
        this.metricDef = new MetricDefinition("my-metric");
        this.metricDefException = new MetricDefinition("metric-that-causes-exception");

        val detectorResource = new DetectorResource(
                DETECTOR_UUID_EWMA.toString(),
                new ModelTypeResource(DETECTOR_TYPE_EWMA),
                true);
        this.detectorResources = new DetectorResources(Collections.singletonList(detectorResource));

        val updatedDetectorsResource = new DetectorResource(
                DETECTOR_UUID_EWMA.toString(),
                new ModelTypeResource(DETECTOR_TYPE_EWMA),
                true);
        this.updatedDetectorResources = new DetectorResources(Collections.singletonList(updatedDetectorsResource));
    }

    private void initTestObjects_findLatestModel() {
        val ewmaParams = new HashMap<String, Object>();
        ewmaParams.put("alpha", 0.2);
        ewmaParams.put("weakSigmas", 2.0);
        ewmaParams.put("strongSigmas", 4.0);

        this.modelResource_ewma = new ModelResource();
        modelResource_ewma.setParams(ewmaParams);
        modelResource_ewma.setDetectorType(new ModelTypeResource(DETECTOR_TYPE_EWMA));

        this.detector = new ForecastingDetector(
                DETECTOR_UUID_EWMA,
                new EwmaPointForecaster(),
                new ExponentialWelfordIntervalForecaster(),
                AnomalyType.TWO_TAILED);
    }

    private void initDependencies() {
        when(connector.findDetectors(metricDef))
                .thenReturn(detectorResources);
        when(connector.findDetectors(metricDefException))
                .thenThrow(new DetectorRetrievalException("Error finding detectors", new IOException()));
        when(connector.findUpdatedDetectors(1))
                .thenReturn(updatedDetectorResources);
        when(connector.findLatestModel(DETECTOR_UUID_EWMA))
                .thenReturn(modelResource_ewma);
        when(connector.findLatestModel(DETECTOR_UUID_MISSING_DETECTOR))
                .thenThrow(new DetectorNotFoundException("No models found"));
        when(connector.findLatestModel(DETECTOR_UUID_EXCEPTION))
                .thenThrow(new DetectorRetrievalException("Error finding latest model", new IOException()));

        when(legacyDetectorFactory.createDetector(any(UUID.class), any(ModelResource.class)))
                .thenReturn(detector);
    }
}
