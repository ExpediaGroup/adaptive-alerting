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

import com.expedia.adaptivealerting.anomdetect.detect.DetectorBuilder;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorDocument;
import com.expedia.adaptivealerting.anomdetect.mapper.Detector;
import com.expedia.adaptivealerting.anomdetect.mapper.DetectorMapping;
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
import static org.mockito.Mockito.when;

@Slf4j
public final class DefaultDetectorSourceTest {
    private static final UUID DETECTOR_UUID_EWMA = UUID.randomUUID();
    private static final UUID DETECTOR_UUID_MISSING_DETECTOR = UUID.randomUUID();
    private static final UUID DETECTOR_UUID_EXCEPTION = UUID.randomUUID();

    private DefaultDetectorSource sourceUnderTest;

    @Mock
    private DetectorClient detectorClient;

    @Mock
    private DetectorBuilder detectorBuilder;

    private DetectorDocument[] updatedDetectorDocuments;
    private DetectorDocument detectorDocument_ewma;
    private DetectorMapping detectorMapping;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
        this.sourceUnderTest = new DefaultDetectorSource(detectorClient, detectorBuilder);
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

    // TODO EWMA type is a legacy type, and I'm not yet supporting it in the detector source.
//    @Test
//    public void testFindDetector_ewma() {
//        val result = sourceUnderTest.findDetector(DETECTOR_UUID_EWMA);
//        assertNotNull(result);
//        assertEquals(DETECTOR_UUID_EWMA, result.getUuid());
//    }

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
        val doc = new DetectorDocument()
                .setUuid(DETECTOR_UUID_EWMA)
                .setEnabled(true)
                .setType("ewma-detector")
                .setDetectorConfig(new HashMap<>())
                .setCreatedBy("kashah")
                .setLastUpdateTimestamp(new Date());
        this.updatedDetectorDocuments = new DetectorDocument[1];
        updatedDetectorDocuments[0] = doc;
        this.detectorMapping = new DetectorMapping()
                .setDetector(new Detector(
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

//        this.detector = new ForecastingOutlierDetector(
//                DETECTOR_UUID_EWMA,
//                new EwmaPointForecaster(),
//                new ExponentialWelfordIntervalForecaster(),
//                AnomalyType.TWO_TAILED);
    }

    private void initDependencies() {
        when(detectorClient.findUpdatedDetectorDocuments(1))
                .thenReturn(Arrays.asList(updatedDetectorDocuments));
        when(detectorClient.findDetectorDocument(DETECTOR_UUID_EWMA))
                .thenReturn(detectorDocument_ewma);
        when(detectorClient.findDetectorDocument(DETECTOR_UUID_MISSING_DETECTOR))
                .thenThrow(new DetectorException("No models found"));
        when(detectorClient.findDetectorDocument(DETECTOR_UUID_EXCEPTION))
                .thenThrow(new DetectorException("Error finding latest model", new IOException()));
        when(detectorClient.findUpdatedDetectorMappings(1))
                .thenReturn(Collections.singletonList(this.detectorMapping));
    }
}
