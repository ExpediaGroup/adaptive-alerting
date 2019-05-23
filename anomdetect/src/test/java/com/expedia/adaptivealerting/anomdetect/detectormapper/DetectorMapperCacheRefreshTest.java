package com.expedia.adaptivealerting.anomdetect.detectormapper;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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

        UUID updateId1 = UUID.randomUUID();
        UUID updateId2 = UUID.randomUUID();
        List<DetectorMapping> detectorIdsOfDisabledMappings = new ArrayList<DetectorMapping>();
        detectorIdsOfDisabledMappings.add(new DetectorMapping().setDetector(new Detector(updateId1)).setEnabled(false));
        detectorIdsOfDisabledMappings.add(new DetectorMapping().setDetector(new Detector(updateId2)).setEnabled(false));

        List<Detector> mappings = new ArrayList<>();
        mappings.add(new Detector(UUID.randomUUID()));
        mappings.add(new Detector(updateId1));
        mappings.add(new Detector(updateId2));

        CacheUtil.getDetectorIds(mappings);

        detectorMapperCache.put("metricKey", mappings);

        assertTrue(detectorMapperCache.get("metricKey").contains(new Detector(updateId1)));

        detectorMapperCache.removeDisabledDetectorMappings(detectorIdsOfDisabledMappings);

        assertFalse(detectorMapperCache.get("metricKey").contains(new Detector(updateId1)));
        assertFalse(detectorMapperCache.get("metricKey").contains(new Detector(updateId2)));

    }

    @Test
    public void updateTest() throws IOException {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        List<DetectorMapping> newDetectorMappings = mapper.readValue(
                new File(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("testDetectorMapping.json")).getFile()),
                new TypeReference<List<DetectorMapping>>() {
                });


        String notMatchingMetricKey = CacheUtil.getKey(ImmutableMap.of("lob", "flight", "pos", "expedia.com"));
        String matchingMetricKey = CacheUtil.getKey(ImmutableMap.of("lob", "hotels", "pos", "expedia.com"));

        Detector d = new Detector(UUID.fromString("2c49ba26-1a7d-43f4-b70c-c6644a2c1689"));
        List<Detector> detectors = Collections.singletonList(d);

        detectorMapperCache.put(notMatchingMetricKey, detectors);
        detectorMapperCache.put(matchingMetricKey, detectors);

        detectorMapperCache.invalidateMetricsWithOldDetectorMappings(newDetectorMappings);


        assertTrue(detectorMapperCache.get(notMatchingMetricKey).contains(d));
        assertFalse(detectorMapperCache.get(matchingMetricKey).contains(d));
        assertTrue(detectorMapperCache.get(matchingMetricKey).isEmpty());


    }

}
