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
package com.expedia.adaptivealerting.anomdetect.detectormapper;

import com.codahale.metrics.MetricRegistry;
import com.expedia.adaptivealerting.anomdetect.DetectorSource;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.TagCollection;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import org.hamcrest.collection.IsMapContaining;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link DetectorMapper} unit test.
 */
public final class DetectorMapperTest {
    private DetectorMapper detectorMapper;
    private int detectorMappingCacheUpdatePeriod = 5;
    @Mock
    private DetectorSource detectorSource;

    @Mock
    private DetectorMapperCache cache;

    @Mock
    private Config config;

    private List<Map<String, String>> tags = new ArrayList<>();
    private List<Map<String, String>> tags_cantRetrieve = new ArrayList<>();
    private List<Map<String, String>> tag_bigList = new ArrayList<>();
    private List<Map<String, String>> listOfMetricTags;


    private DetectorMatchResponse detectorMatchResponse;
    private DetectorMatchResponse detectorMatchResponse_withMoreLookupTime;
    private DetectorMatchResponse emptyDetectorMatchResponse;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
        this.detectorMapper = new DetectorMapper(detectorSource, cache, detectorMappingCacheUpdatePeriod);
    }

    @Test
    public void testConstructorInjection() {
        assertSame(detectorSource, detectorMapper.getDetectorSource());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testModelServiceConnectorNotNull() {
        new DetectorMapper(null, config, new MetricRegistry());
    }

    @Test
    public void testMap_metricDataWithDetectors() {
        boolean results = detectorMapper.isSuccessfulDetectorMappingLookup(tags);
        int batchSize = detectorMapper.optimalBatchSize();
        assertEquals(0, batchSize);
        assertTrue(results);

        results = detectorMapper.isSuccessfulDetectorMappingLookup(tag_bigList);
        batchSize = detectorMapper.optimalBatchSize();
        assertEquals(80, batchSize);
        assertTrue(results);

    }

    @Test
    public void testMap_metricDataWithoutDetectors() {
        final boolean results = detectorMapper.isSuccessfulDetectorMappingLookup(tags_cantRetrieve);
        final int batchSize = detectorMapper.optimalBatchSize();
        assertEquals(0, batchSize);
        assertFalse(results);

    }

    private void initTestObjects() {
        this.tags.add(ImmutableMap.of(
                "org_id", "1",
                "mtype", "count",
                "unit", "",
                "what", "bookings",
                "interval", "5"));
        this.tag_bigList.add(ImmutableMap.of("asdfasd", "gauge",
                "fasdfdsfas", "metric"));
        this.tags_cantRetrieve.add(ImmutableMap.of(
                "random", "one",
                "random3", "two"
        ));

        ArrayList<Detector> detectors = new ArrayList<>();
        detectors.add(new Detector(UUID.fromString("fe1a2366-a73e-4c9d-9186-474e60df6de8")));
        detectors.add(new Detector(UUID.fromString("65eea7d8-8ec3-4f8a-ab2c-7a9dc873723d")));

        Map<Integer, List<Detector>> groupedDetectorsBySearchIndex = ImmutableMap.of(0, detectors);

        this.detectorMatchResponse = new DetectorMatchResponse(groupedDetectorsBySearchIndex, 4);
        this.detectorMatchResponse_withMoreLookupTime = new DetectorMatchResponse(groupedDetectorsBySearchIndex, 400);

        this.emptyDetectorMatchResponse = null;
    }

    private void initDependencies() {
        when(detectorSource.findMatchingDetectorMappings(tags)).thenReturn(detectorMatchResponse);
        when(detectorSource.findMatchingDetectorMappings(tag_bigList)).thenReturn(detectorMatchResponse_withMoreLookupTime);
        when(detectorSource.findMatchingDetectorMappings(tags_cantRetrieve)).thenReturn(emptyDetectorMatchResponse);

        when(config.getInt("detector-mapping-cache-update-period")).thenReturn(detectorMappingCacheUpdatePeriod);
    }

    private void initTagsFromFile() throws IOException {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.listOfMetricTags = mapper.readValue(
                new File(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("dummyListOfMetricTags.json")).getFile()),
                new TypeReference<List<Map<String, String>>>() {
                });


        DetectorMatchResponse detectorMatchResponse = mapper.readValue(
                new File(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("groupedDetectors.json")).getFile()),
                DetectorMatchResponse.class);

        when(detectorSource.findMatchingDetectorMappings(listOfMetricTags)).thenReturn(detectorMatchResponse);
    }

    @Test
    public void testGetDetectorsFromCache() throws IOException {

        //testing detector Mapper with actual cache
        this.detectorMapper = new DetectorMapper(detectorSource, config, new MetricRegistry());

        this.initTagsFromFile();
        //populate cache
        detectorMapper.isSuccessfulDetectorMappingLookup(listOfMetricTags);

        Map<String, List<Detector>> detectorResults = new HashMap<>();

        listOfMetricTags.forEach(tags -> {
            MetricData metricData = new MetricData(new MetricDefinition(new TagCollection(tags)), 0.0, 1L);

            List detector = detectorMapper.getDetectorsFromCache(metricData.getMetricDefinition());
            if (!detector.isEmpty())
                detectorResults.put(CacheUtil.getKey(tags), detector);
        });

        assertThat(detectorResults.size(), is(3));
        assertThat(detectorResults, IsMapContaining.hasEntry("key->DvFWUhv25h,name->61EKBCrwvI", Collections.singletonList(new Detector(UUID.fromString("2c49ba26-1a7d-43f4-b70c-c6644a2c1689")))));
        assertThat(detectorResults, IsMapContaining.hasEntry("key->dAqbZZVPZ8,name->fbWTiRlxkt", Collections.singletonList(new Detector(UUID.fromString("5eaa54e9-7406-4a1d-bd9b-e055eca1a423")))));
        assertThat(detectorResults, IsMapContaining.hasEntry("name->hiw,region->us-west-2", Collections.singletonList(new Detector(UUID.fromString("d86b798c-cfee-4a2c-a17a-aa2ba79ccf51")))));

    }

    @Test
    public void detectorCacheUpdateTest() {

        DetectorMapping disabledDetectorMapping = new DetectorMapping().setDetector(new Detector(UUID.fromString("2c49ba26-1a7d-43f4-b70c-c6644a2c1689"))).setEnabled(false);
        DetectorMapping modifiedDetectorMapping = new DetectorMapping().setDetector(new Detector(UUID.fromString("4d49ba26-1a7d-43f4-b70c-ee644a2c1689"))).setEnabled(true);
        List<DetectorMapping> updateDetectorMappings = Arrays.asList(disabledDetectorMapping, modifiedDetectorMapping);


        when(detectorSource.findUpdatedDetectorMappings(60)).thenReturn(updateDetectorMappings);
        doAnswer((e) -> null).when(cache).removeDisabledDetectorMappings(anyList());
        doAnswer((e) -> null).when(cache).invalidateMetricsWithOldDetectorMappings(anyList());

        detectorMapper.detectorMappingCacheSync(System.currentTimeMillis()+60000);

        verify(cache).removeDisabledDetectorMappings(Collections.singletonList(disabledDetectorMapping));
        verify(cache).invalidateMetricsWithOldDetectorMappings(Collections.singletonList(modifiedDetectorMapping));


    }
}
