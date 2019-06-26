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
package com.expedia.adaptivealerting.anomdetect.detectorsource;

import com.expedia.adaptivealerting.anomdetect.Detector;
import com.expedia.adaptivealerting.anomdetect.detectormapper.DetectorMapping;
import com.expedia.adaptivealerting.anomdetect.outlier.AnomalyType;
import com.expedia.adaptivealerting.anomdetect.outlier.ForecastingOutlierDetector;
import com.expedia.adaptivealerting.anomdetect.outlier.forecast.interval.ExponentialWelfordIntervalForecaster;
import com.expedia.adaptivealerting.anomdetect.outlier.forecast.point.EwmaPointForecaster;
import com.expedia.adaptivealerting.anomdetect.detectorsource.legacy.LegacyDetectorFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Slf4j
public final class DefaultDetectorSourceTest {
    private static final UUID DETECTOR_UUID_EWMA = UUID.randomUUID();
    private static final UUID DETECTOR_UUID_MISSING_DETECTOR = UUID.randomUUID();
    private static final UUID DETECTOR_UUID_EXCEPTION = UUID.randomUUID();

    private DefaultDetectorSource sourceUnderTest;

    @Mock
    private DetectorClient connector;

    @Mock
    private LegacyDetectorFactory legacyDetectorFactory;

    private DetectorDocument[] updatedDetectorDocuments;
    private DetectorDocument detectorDocument_ewma;
    private Detector detector;
    private DetectorMapping detectorMapping;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
        this.sourceUnderTest = new DefaultDetectorSource(connector, legacyDetectorFactory);
    }

    @Test
    public void testFindUpdatedDetectorUuids() {
        val results = sourceUnderTest.findUpdatedDetectors(1);
        assertEquals(1, results.size());

        val result = results.get(0);
        assertEquals(DETECTOR_UUID_EWMA, result);
    }

    @Test
    public void testFindUpdatedDetectorMappings() {
        val results = sourceUnderTest.findUpdatedDetectorMappings(1);
        assertEquals(1, results.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindUpdatedDetectorMappingsFail() {
        sourceUnderTest.findUpdatedDetectorMappings(-1);
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

    @Test(expected = DetectorException.class)
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
        val detectorResource = new DetectorDocument()
                .setUuid(DETECTOR_UUID_EWMA.toString())
                .setEnabled(true)
                .setType("ewma-detector")
                .setDetectorConfig(new HashMap<>())
                .setCreatedBy("kashah")
                .setLastUpdateTimestamp(new Date());
        this.updatedDetectorDocuments = new DetectorDocument[1];
        updatedDetectorDocuments[0] = detectorResource;
        this.detectorMapping = new DetectorMapping()
                .setDetector(new com.expedia.adaptivealerting.anomdetect.detectormapper.Detector(
                        UUID.fromString("2c49ba26-1a7d-43f4-b70c-c6644a2c1689")))
                .setEnabled(false);
    }

    private void initTestObjects_findLatestModel() {
        val ewmaParams = new HashMap<String, Object>();
        ewmaParams.put("alpha", 0.2);
        ewmaParams.put("weakSigmas", 2.0);
        ewmaParams.put("strongSigmas", 4.0);

        Map<String, Object> detectorParams = new HashMap<>();
        detectorParams.put("params", ewmaParams);

        this.detectorDocument_ewma = new DetectorDocument();
        detectorDocument_ewma.setDetectorConfig(new HashMap<>());
        detectorDocument_ewma.setDetectorConfig(detectorParams);
        detectorDocument_ewma.setType("ewma-detector");

        this.detector = new ForecastingOutlierDetector(
                DETECTOR_UUID_EWMA,
                new EwmaPointForecaster(),
                new ExponentialWelfordIntervalForecaster(),
                AnomalyType.TWO_TAILED);
    }

    private void initDependencies() {
        when(connector.findUpdatedDetectorDocuments(1))
                .thenReturn(Arrays.asList(updatedDetectorDocuments));
        when(connector.findDetectorDocument(DETECTOR_UUID_EWMA))
                .thenReturn(detectorDocument_ewma);
        when(connector.findDetectorDocument(DETECTOR_UUID_MISSING_DETECTOR))
                .thenThrow(new DetectorException("No models found"));
        when(connector.findDetectorDocument(DETECTOR_UUID_EXCEPTION))
                .thenThrow(new DetectorException("Error finding latest model", new IOException()));
        when(connector.findUpdatedDetectorMappings(1))
                .thenReturn(Collections.singletonList(this.detectorMapping));

        when(legacyDetectorFactory.createDetector(any(UUID.class), any(DetectorDocument.class)))
                .thenReturn(detector);
    }
}
