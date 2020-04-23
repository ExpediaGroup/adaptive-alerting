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
package com.expedia.adaptivealerting.anomdetect.mapper;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.expedia.adaptivealerting.anomdetect.util.TestFileHelper.getResourceAsFile;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class DetectorMapperCacheRefreshTest {

    private DetectorMapperCache detectorMapperCache;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.detectorMapperCache = new DetectorMapperCache(new MetricRegistry());
    }

    @Test
    public void removeTest() {

        String updateId1 = "774a5e3a-9ef4-48c7-a1e8-f09a623f45fb";
        String updateId2 = "c5e30d0f-c000-47a7-b411-b4e7fddbed88";
        List<DetectorMapping> detectorMappings = new ArrayList<DetectorMapping>();
        detectorMappings.add(new DetectorMapping().setDetector(buildDetector(updateId1)).setEnabled(false));
        detectorMappings.add(new DetectorMapping().setDetector(buildDetector(updateId2)).setEnabled(false));


        List<Detector> detectors = new ArrayList<>();
        detectors.add(buildDetector("936e9f6b-4f83-4f09-bd5e-dee62657e5e2"));
        detectors.add(buildDetector(updateId1));
        detectors.add(buildDetector(updateId2));

        CacheUtil.getDetectors(detectors);

        detectorMapperCache.put("metricKey", detectors);

        assertTrue(detectorMapperCache.get("metricKey").contains(buildDetector(updateId1)));

        detectorMapperCache.removeDisabledDetectorMappings(detectorMappings);
        assertFalse(detectorMapperCache.get("metricKey").contains(buildDetector(updateId1)));
        assertFalse(detectorMapperCache.get("metricKey").contains(buildDetector(updateId2)));
    }

    @Test
    public void updateTest() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        List<DetectorMapping> newDetectorMappings = mapper.readValue(
                getResourceAsFile("testDetectorMapping.json"),
                new TypeReference<List<DetectorMapping>>() {
                });

        String notMatchingMetricKey = CacheUtil.getKey(ImmutableMap.of("lob", "flight", "pos", "expedia.com"));
        String matchingMetricKey = CacheUtil.getKey(ImmutableMap.of("lob", "hotels", "pos", "expedia.com"));

        Detector d = buildDetector("2c49ba26-1a7d-43f4-b70c-c6644a2c1689");
        List<Detector> detectors = Collections.singletonList(d);

        detectorMapperCache.put(notMatchingMetricKey, detectors);
        detectorMapperCache.put(matchingMetricKey, detectors);

        detectorMapperCache.invalidateMetricsWithOldDetectorMappings(newDetectorMappings);


        assertTrue(detectorMapperCache.get(notMatchingMetricKey).contains(d));
        assertFalse(detectorMapperCache.get(matchingMetricKey).contains(d));
        assertTrue(detectorMapperCache.get(matchingMetricKey).isEmpty());

    }

    private Detector buildDetector(String detectorUuid) {
        return new Detector("cid", UUID.fromString(detectorUuid));
    }

}
