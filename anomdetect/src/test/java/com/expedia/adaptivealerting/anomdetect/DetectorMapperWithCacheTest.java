package com.expedia.adaptivealerting.anomdetect;

import com.expedia.adaptivealerting.anomdetect.comp.DetectorSource;
import com.expedia.adaptivealerting.anomdetect.mapper.CacheUtil;
import com.expedia.adaptivealerting.anomdetect.mapper.Detector;
import com.expedia.adaptivealerting.anomdetect.mapper.DetectorMatchResponse;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.TagCollection;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.collection.IsMapContaining;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertSame;

/**
 * {@link DetectorMapper} unit test.
 */
public class DetectorMapperWithCacheTest {

    List<Map<String, String>> listOfMetricTags;

    @InjectMocks
    private DetectorMapper detectorMapper;

    @Mock
    private DetectorSource detectorSource;

    @Before
    public void initMocks() {
//        this.detectorMapper = new DetectorMapper(detectorSource);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testConstructorInjection() {
        assertSame(detectorSource, detectorMapper.getDetectorSource());
    }

    @Test(expected = AssertionError.class)
    public void testModelServiceConnectorNotNull() {
        new DetectorMapper(null);
    }

    private void initDependencies() throws IOException {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.listOfMetricTags = mapper.readValue(
                new File(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("dummyListOfMetricTags.json")).getFile()),
                new TypeReference<List<Map<String, String>>>() {
                });


        DetectorMatchResponse detectorMatchResponse = mapper.readValue(
                new File(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("testDetectorMapping.json")).getFile()),
                DetectorMatchResponse.class);

        Mockito.when(detectorSource.findMatchingDetectorMappings(listOfMetricTags)).thenReturn(detectorMatchResponse);
    }

    @Test
    public void testDoCacheLookupAndSendToOutputTopic() throws IOException {
        this.initDependencies();
        detectorMapper.isSuccessfulDetectorMappingLookup(listOfMetricTags);

        Map<String, List<Detector>> detectorResults = new HashMap<>();

        listOfMetricTags.forEach(tags -> {
            MetricData metricData = new MetricData(new MetricDefinition(new TagCollection(tags)), 0.0, 1L);

            List detector = detectorMapper.getDetectorsFromCache(metricData);
            if (!detector.isEmpty())
                detectorResults.put(CacheUtil.getKey(tags), detector);
        });

        assertThat(detectorResults.size(), is(3));
        assertThat(detectorResults, IsMapContaining.hasEntry("key=DvFWUhv25h,name=61EKBCrwvI", Collections.singletonList(new Detector(UUID.fromString("2c49ba26-1a7d-43f4-b70c-c6644a2c1689")))));
        assertThat(detectorResults, IsMapContaining.hasEntry("key=dAqbZZVPZ8,name=fbWTiRlxkt", Collections.singletonList(new Detector(UUID.fromString("5eaa54e9-7406-4a1d-bd9b-e055eca1a423")))));
        assertThat(detectorResults, IsMapContaining.hasEntry("name=hiw,region=us-west-2", Collections.singletonList(new Detector(UUID.fromString("d86b798c-cfee-4a2c-a17a-aa2ba79ccf51")))));


    }
}
