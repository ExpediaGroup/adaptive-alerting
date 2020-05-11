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

import com.expedia.adaptivealerting.anomdetect.detect.Detector;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorContainer;
import com.expedia.adaptivealerting.anomdetect.mapper.DetectorMapping;
import com.expedia.adaptivealerting.anomdetect.mapper.DetectorMatchResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@Slf4j
public final class DefaultDetectorSourceTest {
    private static final UUID DETECTOR_UUID = UUID.randomUUID();
    private static final UUID DETECTOR_UUID_MISSING_DETECTOR = UUID.randomUUID();
    private static final UUID DETECTOR_UUID_EXCEPTION = UUID.randomUUID();

    private DefaultDetectorSource sourceUnderTest;

    @Mock
    private DetectorClient detectorClient;

    @Mock
    private DetectorFactory detectorFactory;

    private List<Map<String, String>> metricTags;
    private DetectorMatchResponse detectorMatchResponse;
    private DetectorDocument[] updatedDetectorDocuments;
    private DetectorMapping detectorMapping;

    @Mock
    private DetectorDocument detectorDocument;

    @Mock
    private Detector detector;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
        this.sourceUnderTest = new DefaultDetectorSource(detectorClient, detectorFactory);
    }

    @Test
    public void testFindDetectorMappings() {
        val result = sourceUnderTest.findDetectorMappings(metricTags);
        assertSame(detectorMatchResponse, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindDetectorMappings_nullTags() {
        sourceUnderTest.findDetectorMappings(null);
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
    public void testFindDetectorMappingsUpdatedSince() {
        val results = sourceUnderTest.findDetectorMappingsUpdatedSince(0);
        assertEquals(1, results.size());
    }

    @Test
    public void testGetEnabledDetectorMappingCount() {
        val result = sourceUnderTest.getEnabledDetectorMappingCount();
        assertEquals(10, result);
    }

    @Test
    public void testFindDetectorMappingByUuid() {
        val results = sourceUnderTest.findDetectorMappingByUuid(UUID.randomUUID());
        assertEquals(detectorMapping, results);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindDetectorMappingByUuidFail() {
        sourceUnderTest.findDetectorMappingByUuid(null);
    }

    @Test
    public void testFindUpdatedDetectors() {
        val results = sourceUnderTest.findUpdatedDetectors(1);
        assertEquals(1, results.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindUpdatedDetectors_invalidTimePeriod() {
        sourceUnderTest.findUpdatedDetectors(0);
    }

    @Test
    public void testBuildDetectorContainer() {
        val result = sourceUnderTest.findDetector(DETECTOR_UUID);
        assertNotNull(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindDetector_nullMeta() {
        sourceUnderTest.findDetector(null);
    }

    @Test
    public void testFindDetector_nullDetectorBuilt() {
        when(detectorClient.findDetectorDocument(DETECTOR_UUID)).thenReturn(detectorDocument);
        when(detectorFactory.buildDetector(detectorDocument)).thenReturn(null);
        DetectorContainer detector = sourceUnderTest.findDetector(DETECTOR_UUID);
        assertNull(detector);
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
        this.metricTags = new ArrayList<>();
        this.detectorMatchResponse = new DetectorMatchResponse();
        initTestObjects_findDetector();
        initTestObjects_findUpdatedDetectors();
    }

    private void initTestObjects_findDetector() {
        when(detectorDocument.getType()).thenReturn("some-detector-type");
    }

    private void initTestObjects_findUpdatedDetectors() {
        this.detectorMapping = new DetectorMapping()
                .setDetector(new com.expedia.adaptivealerting.anomdetect.mapper.Detector(
                        "ad-manager", UUID.fromString("2c49ba26-1a7d-43f4-b70c-c6644a2c1689")))
                .setEnabled(false);

        this.updatedDetectorDocuments = new DetectorDocument[1];
        updatedDetectorDocuments[0] = detectorDocument;
    }

    private void initDependencies() {
        when(detectorClient.findMatchingDetectorMappings(anyList()))
                .thenReturn(detectorMatchResponse);
        when(detectorClient.findUpdatedDetectorDocuments(1))
                .thenReturn(Arrays.asList(updatedDetectorDocuments));
        when(detectorClient.findDetectorDocument(DETECTOR_UUID))
                .thenReturn(detectorDocument);
        when(detectorClient.findDetectorDocument(DETECTOR_UUID_MISSING_DETECTOR))
                .thenThrow(new DetectorException("No detectors found"));
        when(detectorClient.findDetectorDocument(DETECTOR_UUID_EXCEPTION))
                .thenThrow(new DetectorException("Error finding latest model", new IOException()));
        when(detectorClient.findUpdatedDetectorMappings(1))
                .thenReturn(Collections.singletonList(this.detectorMapping));
        when(detectorClient.findDetectorMappingByUuid(any(UUID.class)))
                .thenReturn(this.detectorMapping);
        when(detectorFactory.buildDetector(any(DetectorDocument.class))).thenReturn(detector);
        when(detectorClient.findDetectorMappingsUpdatedSince(anyLong()))
                .thenReturn(Collections.singletonList(this.detectorMapping));
        when(detectorClient.getEnabledDetectorMappingCount())
        .thenReturn(10L);
    }
}
