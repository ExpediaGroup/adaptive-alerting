package com.expedia.adaptivealerting.anomdetect.mapper;

import com.codahale.metrics.MetricRegistry;
import com.expedia.adaptivealerting.anomdetect.source.DetectorSource;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.TagCollection;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import org.hamcrest.collection.IsMapContaining;
import org.hamcrest.number.IsCloseTo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import lombok.val;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.expedia.adaptivealerting.anomdetect.util.TestFileHelper.getResourceAsFile;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * {@link DetectorMapper} unit test.
 */
public final class DetectorMapperTest {

    private DetectorMapper detectorMapper;
    private MetricRegistry metricRegistry;
    private DetectorMapper detectorMapperFilterDisabled;
    private MetricRegistry metricRegistryFilterDisabled;
    private int detectorMappingCacheUpdatePeriod = 5;

    @Mock
    private DetectorSource detectorSource;

    @Mock
    private DetectorSource bigDetectorSource;

    @Mock
    private DetectorSource emptyDetectorSource;

    @Mock
    private DetectorSource smallDetectorSource;

    @Mock
    private DetectorMapperCache cache;

    @Mock
    private DetectorMapperCache bigCache;

    @Mock
    private DetectorMapperCache emptyCache;

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
        this.metricRegistry = new MetricRegistry();
        this.detectorMapper = new DetectorMapper(detectorSource, cache, detectorMappingCacheUpdatePeriod,
                true, metricRegistry);
        this.metricRegistryFilterDisabled = new MetricRegistry();
        this.detectorMapperFilterDisabled = new DetectorMapper(detectorSource, cache, detectorMappingCacheUpdatePeriod,
                false, metricRegistryFilterDisabled);
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

    @Test
    public void testBloomFilter() {
        // bigDetectorSource
        when(bigDetectorSource.getEnabledDetectorMappingCount()).thenReturn(10_001L);
        when(bigDetectorSource.findDetectorMappingsUpdatedSince(anyLong()))
            .thenAnswer(new Answer() {
                public Object answer(InvocationOnMock invocation) {
                    Object[] args = invocation.getArguments();
                    invocation.getMock();
                    Long lastUpdatedTime = (long) args[0];
                    return generateDetectorMappings(lastUpdatedTime, 10_001);
                    }
                });
        MetricRegistry bigMetricRegistry = new MetricRegistry();
        DetectorMapper bigDetectorMapper = new DetectorMapper(bigDetectorSource, bigCache, detectorMappingCacheUpdatePeriod, true, bigMetricRegistry);
        // Out of 10,000 metrics, ca are mapped (every 33rd was skipped)
        // the Bloom Filter should accurately identify all positive cases, but may
        // have some false positives. The actual rate of false positives in the
        // test should not exceed the configured false positive threshold.
        int bloomFilterTruePositiveCount = 0;
        int bloomFilterFalsePositiveCount = 0;
        int bloomFilterTrueNegativeCount = 0;
        int bloomFilterFalseNegativeCount = 0;
        for (int i = 0; i < 10_001; i++) {
            MetricDefinition metricDefinition = new MetricDefinition(new TagCollection(generateTagsForIndex(i)));
            Boolean metricMightBeMapped = bigDetectorMapper.metricMightBeMapped(metricDefinition);
            if (i % 33 == 0) {
                if (metricMightBeMapped) {
                    bloomFilterFalsePositiveCount++;
                } else {
                    bloomFilterTrueNegativeCount++;
                }
            } else {
                if (metricMightBeMapped) {
                    bloomFilterTruePositiveCount++;
                } else {
                    bloomFilterFalseNegativeCount++;
                }
            }
        }
        Long mappingFilterSizeGaugeValue = (long) bigMetricRegistry.getGauges().get("detector-mapper.filter-size").getValue();
        assertEquals(110_001L, mappingFilterSizeGaugeValue.longValue());
        Long mappingFilterItemCountGaugeValue = (long) bigMetricRegistry.getGauges().get("detector-mapper.filter-item-count").getValue();
        assertTrue(mappingFilterItemCountGaugeValue.longValue() > 9696L);
        Double filterSaturation = (double) bigMetricRegistry.getGauges().get("detector-mapper.filter-saturation").getValue();
        assertThat(filterSaturation, new IsCloseTo(0.088, 0.001));
        assertEquals(9697, bloomFilterTruePositiveCount);
        assertTrue(bloomFilterFalsePositiveCount <= 10_001 * bigDetectorMapper.FILTER_FALSE_POSITIVE_PROB_THRESHOLD);
        assertTrue(
                bloomFilterTrueNegativeCount >= 303 - (10_001 * bigDetectorMapper.FILTER_FALSE_POSITIVE_PROB_THRESHOLD));
        assertEquals(0, bloomFilterFalseNegativeCount);
        System.out.println(String.format("Bloom Filter False Positive Threshold: %s%s, Actual: %s%s",
                Math.floor(bigDetectorMapper.FILTER_FALSE_POSITIVE_PROB_THRESHOLD * 10_001) / 100, "%",
                Math.floor((bloomFilterFalsePositiveCount / 10_001.0) * 10_000) / 100, "%"));
    }

    // @Test(expected = RuntimeException.class)
    // public void isSuccessfulDetectorMappingLookup_fail() {
    // when(detectorSource.findDetectorMappings(anyList()).thenThrow(new
    // RuntimeException());
    // detectorMapper.isSuccessfulDetectorMappingLookup(tag_bigList);
    // }

    @Test
    public void testDisabledBloomFilter() {
        // when(config.getBoolean("detector-mapping-filter-enabled")).thenReturn(false);

        for (int i = 0; i < 10_000; i++) {
            MetricDefinition metricDefinition = new MetricDefinition(new TagCollection(generateTagsForIndex(i)));
            detectorMapperFilterDisabled.metricMightBeMapped(metricDefinition);
        }
        Long mappingFilterSizeGaugeValue = (long) metricRegistryFilterDisabled.getGauges().get("detector-mapper.filter-size").getValue();
        assertEquals(100_000L, mappingFilterSizeGaugeValue.longValue());
        Long mappingFilterItemCountGaugeValue = (long) metricRegistryFilterDisabled.getGauges().get("detector-mapper.filter-item-count").getValue();
        assertEquals(0L, mappingFilterItemCountGaugeValue.longValue());
        Double filterSaturation = (double) metricRegistryFilterDisabled.getGauges().get("detector-mapper.filter-saturation").getValue();
        assertEquals(new Double(0.0), filterSaturation);
    }

    private void initTestObjects() {
        this.tags
                .add(ImmutableMap.of("org_id", "1", "mtype", "count", "unit", "", "what", "bookings", "interval", "5"));
        this.tag_bigList.add(ImmutableMap.of("asdfasd", "gauge", "fasdfdsfas", "metric"));
        this.tags_cantRetrieve.add(ImmutableMap.of("random", "one", "random3", "two"));

        ArrayList<Detector> detectors = new ArrayList<>();
        detectors.add(buildDetector("cid", "fe1a2366-a73e-4c9d-9186-474e60df6de8"));
        detectors.add(buildDetector("cid", "65eea7d8-8ec3-4f8a-ab2c-7a9dc873723d"));

        Map<Integer, List<Detector>> groupedDetectorsBySearchIndex = ImmutableMap.of(0, detectors);

        this.detectorMatchResponse = new DetectorMatchResponse(groupedDetectorsBySearchIndex, 4);
        this.detectorMatchResponse_withMoreLookupTime = new DetectorMatchResponse(groupedDetectorsBySearchIndex, 400);

        this.emptyDetectorMatchResponse = null;
    }

    private List<DetectorMapping> generateDetectorMappings(Long lastUpdatedTime, int itemCount){
        List<DetectorMapping> detectorMappings = new ArrayList<DetectorMapping>();
        for (int i = lastUpdatedTime.intValue() + 1; i < (lastUpdatedTime + 501) && i < itemCount; i++) {
            DetectorMapping newDetectorMapping = generateDetectorMapping(generateTagsForIndex(i));
            newDetectorMapping.setLastModifiedTimeInMillis(i);
            if (i % 33 != 0) {
                detectorMappings.add(newDetectorMapping);
            } else {
                newDetectorMapping.setEnabled(false);
                detectorMappings.add(newDetectorMapping);
            }
        }
        return detectorMappings;
    }

    private DetectorMapping generateDetectorMapping(Map<String, String> tags) {
        ExpressionTree expression = new ExpressionTree();
        expression.setOperator(Operator.AND);
        List<Operand> operandsList = new ArrayList<>();
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            Operand operand = new Operand();
            operand.setField(new Field(entry.getKey(), entry.getValue()));
            operandsList.add(operand);
        }
        expression.setOperands(operandsList);
        return new DetectorMapping().setDetector(new Detector("cid", UUID.randomUUID())).setEnabled(true)
                .setExpression(expression);
    }

    private Map<String, String> generateTagsForIndex(int index) {
        val tags = new HashMap<String, String>();
        tags.put("service", "service" + Integer.toString(index));
        tags.put("type", "error_count");
        return tags;
    }

    private void initDependencies() {

        when(config.getInt("detector-mapping-cache-update-period")).thenReturn(detectorMappingCacheUpdatePeriod);

        when(detectorSource.getEnabledDetectorMappingCount()).thenReturn(0L);
        when(detectorSource.findDetectorMappingsUpdatedSince(anyLong()))
            .thenReturn(generateDetectorMappings(0L, 0));
        when(detectorSource.findDetectorMappings(tags)).thenReturn(detectorMatchResponse);
        when(detectorSource.findDetectorMappings(tag_bigList)).thenReturn(detectorMatchResponse_withMoreLookupTime);
        when(detectorSource.findDetectorMappings(tags_cantRetrieve)).thenReturn(emptyDetectorMatchResponse);
    }

    private void initTagsFromFile() throws IOException {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.listOfMetricTags = mapper.readValue(getResourceAsFile("dummyListOfMetricTags.json"),
                new TypeReference<List<Map<String, String>>>() {
                });

        DetectorMatchResponse detectorMatchResponse = mapper.readValue(getResourceAsFile("groupedDetectors.json"),
                DetectorMatchResponse.class);

        when(detectorSource.findDetectorMappings(listOfMetricTags)).thenReturn(detectorMatchResponse);
    }

    @Test
    public void testGetDetectorsFromCache() throws IOException {

        // testing detector Mapper with actual cache
        this.detectorMapper = new DetectorMapper(detectorSource, config, new MetricRegistry());

        this.initTagsFromFile();
        // populate cache
        detectorMapper.isSuccessfulDetectorMappingLookup(listOfMetricTags);

        Map<String, List<Detector>> detectorResults = new HashMap<>();

        listOfMetricTags.forEach(tags -> {
            MetricData metricData = new MetricData(new MetricDefinition(new TagCollection(tags)), 0.0, 1L);

            List<Detector> detector = detectorMapper.getDetectorsFromCache(metricData.getMetricDefinition());
            if (!detector.isEmpty())
                detectorResults.put(CacheUtil.getKey(tags), detector);
        });

        assertThat(detectorResults.size(), is(3));
        assertThat(detectorResults, IsMapContaining.hasEntry("key->RHZGV1VodjI1aA==,name->NjFFS0JDcnd2SQ==",
                Collections.singletonList(buildDetector("cid", "2c49ba26-1a7d-43f4-b70c-c6644a2c1689"))));
        assertThat(detectorResults, IsMapContaining.hasEntry("key->ZEFxYlpaVlBaOA==,name->ZmJXVGlSbHhrdA==",
                Collections.singletonList(buildDetector("ad-manager", "5eaa54e9-7406-4a1d-bd9b-e055eca1a423"))));
        assertThat(detectorResults, IsMapContaining.hasEntry("name->aGl3,region->dXMtd2VzdC0y",
                Collections.singletonList(buildDetector("", "d86b798c-cfee-4a2c-a17a-aa2ba79ccf51"))));
    }

    // @Test
    // public void detectorCacheUpdateEmptyTest() {
    //     DetectorMapper emptyCacheDetectorMapper = new DetectorMapper(emptyDetectorSource, emptyCache, detectorMappingCacheUpdatePeriod, true, new MetricRegistry());
        
    //     when(emptyDetectorSource.findDetectorMappingsUpdatedSince(anyLong())).thenReturn(new ArrayList<DetectorMapping>());
    //     emptyCacheDetectorMapper.detectorMappingCacheSync();

    //     verify(emptyCache, never()).removeDisabledDetectorMappings(anyList());
    //     verify(emptyCache, never()).invalidateMetricsWithOldDetectorMappings(anyList());
    // }


    private Detector buildDetector(String consumerId, String detectorUuid) {
        return new Detector(consumerId, UUID.fromString(detectorUuid));
    }

    @Test
    public void testSinglePageDetectorMappingResponse() {
        when(smallDetectorSource.getEnabledDetectorMappingCount()).thenReturn(500L);
        when(smallDetectorSource.findDetectorMappingsUpdatedSince(anyLong()))
        .thenAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                invocation.getMock();
                Long lastUpdatedTime = (long) args[0];
                return generateDetectorMappings(lastUpdatedTime, 500);
                }
            });
        DetectorMapper smallDetectorMapper = new DetectorMapper(smallDetectorSource, config, new MetricRegistry());
        MetricDefinition metricDefinition = new MetricDefinition(new TagCollection(generateTagsForIndex(1)));
        assertTrue(smallDetectorMapper.metricMightBeMapped(metricDefinition));
    }
}