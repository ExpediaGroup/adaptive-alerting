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

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.expedia.adaptivealerting.anomdetect.source.DetectorSource;
import com.expedia.adaptivealerting.anomdetect.util.AssertUtil;
import com.expedia.metrics.MetricDefinition;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.typesafe.config.Config;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Detector mapper finds matching detectors for each incoming
 * {@link MetricDefinition}
 */
@Slf4j
public class DetectorMapper {
    public final double FILTER_FALSE_POSITIVE_PROB_THRESHOLD = 0.01;
    public final long FILTER_GROWTH_SIZE = 100_000L;
    private static final int OPTIMAL_BATCH_SIZE = 80;
    private static final String CK_DETECTOR_CACHE_UPDATE_PERIOD = "detector-mapping-cache-update-period";
    private static final String DETECTOR_MAPPER_FILTER_ENABLED = "detector-mapping-filter-enabled";
    private static final String DETECTOR_MAPPER_ERRORS = "detector-mapper.exceptions";
    private static final String DETECTOR_MAPPER_FILTER_SIZE = "detector-mapper.filter-size";
    private static final String DETECTOR_MAPPER_FILTER_ITEM_COUNT = "detector-mapper.filter-item-count";
    private static final String DETECTOR_MAPPER_FILTER_SATURATION = "detector-mapper.filter-saturation";
    private static final String DETECTOR_MAPPER_FILTER_MIGHTMATCH_COUNT = "detector-mapper.filter-mightmatch-total-count";
    private static final String DETECTOR_MAPPER_FILTER_POSITIVE_COUNT = "detector-mapper.filter-mightmatch-positive-count";
    private static final String DETECTOR_MAPPER_FILTER_NEGATIVE_COUNT = "detector-mapper.filter-mightmatch-negative-count";
    private static final String DETECTOR_MAPPER_LOOKUP_TOTAL_COUNT = "detector-mapper.lookup-total-count";
    private static final String DETECTOR_MAPPER_LOOKUP_TIME = "detector-mapper.lookup-time";
    private static final String DETECTOR_MAPPER_LOOKUP_NO_RESULT_COUNT = "detector-mapper.lookup-noresults-count";

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private AtomicLong lastElasticLookUpLatency = new AtomicLong(-1);

    @Getter
    @NonNull
    private DetectorSource detectorSource;
    private DetectorMapperCache cache;
    private Counter exceptionCounter;
    private Timer mappingLookupTimer;
    private Boolean detectorMappingFilterEnabled;
    private BloomFilter<String> detectorMappingBloomFilter;
    private Long detectorMappingFilterSize = FILTER_GROWTH_SIZE;
    private Long detectorMappingFilterItemCount = 0L;
    private Gauge<Long> mappingFilterSizeGauge = () -> detectorMappingFilterSize;
    private Gauge<Long> mappingFilterItemCountGauge = () -> detectorMappingFilterItemCount;
    private Gauge<Double> mappingFilterSaturationGauge = () -> (double) detectorMappingFilterItemCount / (double) detectorMappingFilterSize;
    private Counter mappingFilterMightMatchTotalCounter;
    private Counter mappingFilterMightMatchPositiveCounter;
    private Counter mappingFilterMightMatchNegativeCounter;
    private Counter mappingLookupTotalCounter;
    private Counter mappingLookupNoResultCounter;
    private int detectorCacheUpdateTimePeriod;
    private long syncedUpTillTime = System.currentTimeMillis();

    public DetectorMapper(DetectorSource detectorSource, DetectorMapperCache cache, int detectorCacheUpdateTimePeriod,
            Boolean detectorMappingFilterEnabled, MetricRegistry metricRegistry) {
        AssertUtil.notNull(detectorSource, "Detector source can't be null");
        this.detectorSource = detectorSource;
        this.cache = cache;
        this.detectorCacheUpdateTimePeriod = detectorCacheUpdateTimePeriod;
        this.detectorMappingFilterEnabled = detectorMappingFilterEnabled;
        this.mappingLookupTimer = metricRegistry.timer(DETECTOR_MAPPER_LOOKUP_TIME);
        this.exceptionCounter = metricRegistry.counter(DETECTOR_MAPPER_ERRORS);
        this.mappingFilterMightMatchTotalCounter = metricRegistry.counter(DETECTOR_MAPPER_FILTER_MIGHTMATCH_COUNT);
        this.mappingFilterMightMatchPositiveCounter = metricRegistry.counter(DETECTOR_MAPPER_FILTER_POSITIVE_COUNT);
        this.mappingFilterMightMatchNegativeCounter = metricRegistry.counter(DETECTOR_MAPPER_FILTER_NEGATIVE_COUNT);
        this.mappingLookupTotalCounter = metricRegistry.counter(DETECTOR_MAPPER_LOOKUP_TOTAL_COUNT);
        this.mappingLookupNoResultCounter = metricRegistry.counter(DETECTOR_MAPPER_LOOKUP_NO_RESULT_COUNT);
        metricRegistry.register(DETECTOR_MAPPER_FILTER_SIZE, mappingFilterSizeGauge);
        metricRegistry.register(DETECTOR_MAPPER_FILTER_ITEM_COUNT, mappingFilterItemCountGauge);
        metricRegistry.register(DETECTOR_MAPPER_FILTER_SATURATION, mappingFilterSaturationGauge);
        if (detectorMappingFilterEnabled) {
            this.initBloomFilter();
        }
        this.initScheduler();
    }

    public DetectorMapper(DetectorSource detectorSource, Config config, MetricRegistry metricRegistry) {
        this(detectorSource, new DetectorMapperCache(metricRegistry), config.getInt(CK_DETECTOR_CACHE_UPDATE_PERIOD),
                config.getBoolean(DETECTOR_MAPPER_FILTER_ENABLED), metricRegistry);
    }

    private void initScheduler() {
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                log.trace("Updating detector mapping cache");
                this.detectorMappingCacheSync();
            } catch (Exception e) {
                log.error("Error updating detectors mapping cache", e);
                exceptionCounter.inc();
            }
        }, detectorCacheUpdateTimePeriod, detectorCacheUpdateTimePeriod, TimeUnit.MINUTES);
    }

    private void initBloomFilter() {
        /*
         * Bloom filters are a memory efficient way to filter out most items that don't
         * exist is a set. By hashing all existing detector mappings in the filter, >99%
         * of metrics that are not mapped be efficiently skipped (without looking them
         * up in the model service). Some false positives will make it past the filter,
         * but as they don't have a mapping, there is no harm done.
         * 
         * initBloomFilter: All detector mappings are retrieved from the model service
         * at startup and added to the Bloom filter. The mapping count is used to
         * determine how many bits are used in the Bloom filter. If the number is too
         * low, the filter will become saturated resulting in an increase in false
         * positives.
         * 
         * If a mapping is added after startup, the new mappings will be added to the
         * filter in detectorMappingCacheSync.
         * 
         */
        final long detectorMappingEnabledCount = this.detectorSource.getEnabledDetectorMappingCount();
        // initialize with double the size,
        this.detectorMappingFilterSize = detectorMappingEnabledCount + FILTER_GROWTH_SIZE;
        this.detectorMappingBloomFilter = BloomFilter.create(Funnels.unencodedCharsFunnel(), detectorMappingFilterSize,
                FILTER_FALSE_POSITIVE_PROB_THRESHOLD);
        int lastPageSize = 500;
        long lastModifiedTime = 0L;
        while (lastPageSize == 500) {
            final List<DetectorMapping> detectorMappings = detectorSource
                    .findDetectorMappingsUpdatedSince(lastModifiedTime);
            lastPageSize = detectorMappings.size();
            log.debug("{} detector mappings received from model service updated since {}.", lastPageSize,
                    lastModifiedTime);
            for (final DetectorMapping detectorMapping : detectorMappings) {
                if (detectorMapping.isEnabled()) {
                    detectorMappingFilterItemCount++;
                    detectorMappingBloomFilter.put(detectorMapping.getKey());
                }
            }
            if (lastPageSize > 0) {
                DetectorMapping lastDetectorMapping = detectorMappings.get(lastPageSize - 1);
                lastModifiedTime = lastDetectorMapping.getLastModifiedTimeInMillis();
            }
        }
    }

    public int optimalBatchSize() {
        if (lastElasticLookUpLatency.longValue() == -1L || lastElasticLookUpLatency.longValue() > 10L) {
            return OPTIMAL_BATCH_SIZE;
        }
        return 0;
    }

    public List<Detector> getDetectorsFromCache(MetricDefinition metricDefinition) {
        String cacheKey = CacheUtil.getKey(metricDefinition.getTags().getKv());
        List<Detector> cachedDetectors = cache.get(cacheKey);
        if (cachedDetectors.isEmpty()) {
            log.debug("No detectors found for key {}", cacheKey);
        } else {
            log.debug("Found detectors {} for key {}", cachedDetectors, cacheKey);
        }
        return cachedDetectors;
    }

    public boolean isSuccessfulDetectorMappingLookup(List<Map<String, String>> cacheMissedMetricTags) {

        mappingLookupTotalCounter.inc(cacheMissedMetricTags.size());
        DetectorMatchResponse matchingDetectorMappings = getMappingsFromElasticSearch(cacheMissedMetricTags);
        if (matchingDetectorMappings != null) {
            lastElasticLookUpLatency.set(matchingDetectorMappings.getLookupTimeInMillis());
            mappingLookupTimer.update(matchingDetectorMappings.getLookupTimeInMillis(), TimeUnit.MILLISECONDS);
            Map<Integer, List<Detector>> groupedDetectorsByIndex = matchingDetectorMappings
                    .getGroupedDetectorsBySearchIndex();
            populateCache(groupedDetectorsByIndex, cacheMissedMetricTags);
            Set<Integer> searchIndexes = groupedDetectorsByIndex.keySet();

            // For metrics with no matching detectors, set matching detectors to empty in
            // cache to avoid repeated cache miss
            int i = 0;
            for (Map<String, String> tags : cacheMissedMetricTags) {
                if (!searchIndexes.contains(i)) {
                    mappingLookupNoResultCounter.inc();
                    String cacheKey = CacheUtil.getKey(tags);
                    cache.put(cacheKey, Collections.emptyList());
                }
                i++;
            }
        } else {
            lastElasticLookUpLatency.set(-2);
        }
        return matchingDetectorMappings != null;
    }

    public void detectorMappingCacheSync() {

        List<DetectorMapping> detectorMappings = detectorSource.findDetectorMappingsUpdatedSince(syncedUpTillTime);

        List<DetectorMapping> disabledDetectorMappings = detectorMappings.stream().filter(dt -> !dt.isEnabled())
                .collect(Collectors.toList());
        if (!disabledDetectorMappings.isEmpty()) {
            cache.removeDisabledDetectorMappings(disabledDetectorMappings);
            log.info("Removing disabled mapping: {}", disabledDetectorMappings);
        }

        List<DetectorMapping> newDetectorMappings = detectorMappings.stream().filter(DetectorMapping::isEnabled)
                .collect(Collectors.toList());
        if (!newDetectorMappings.isEmpty()) {
            cache.invalidateMetricsWithOldDetectorMappings(newDetectorMappings);
            log.info("Invalidating metrics for modified mappings: {}", newDetectorMappings);
        }
        if (!newDetectorMappings.isEmpty() && detectorMappingFilterEnabled) {
            for (DetectorMapping detectorMapping : newDetectorMappings) {
                detectorMappingBloomFilter.put(detectorMapping.getKey());
            }
        }
        if (detectorMappings.size() > 0) {
            DetectorMapping lastDetectorMapping = detectorMappings.get(detectorMappings.size() - 1);
            syncedUpTillTime = lastDetectorMapping.getLastModifiedTimeInMillis();
        }
    }

    private DetectorMatchResponse getMappingsFromElasticSearch(List<Map<String, String>> cacheMissedMetricTags) {
        DetectorMatchResponse matchingDetectorMappings = null;
        try {
            matchingDetectorMappings = detectorSource.findDetectorMappings(cacheMissedMetricTags);
        } catch (RuntimeException e) {
            log.error("Error fetching detector mappings from elastic search", e);
            exceptionCounter.inc();
        }
        return matchingDetectorMappings;
    }

    private void populateCache(Map<Integer, List<Detector>> groupedDetectorsByIndex,
            List<Map<String, String>> cacheMissedMetricTags) {
        groupedDetectorsByIndex.forEach((index, detectors) -> {
            String cacheKey = CacheUtil.getKey(cacheMissedMetricTags.get(index));
            if (!detectors.isEmpty()) {
                cache.put(cacheKey, detectors);
            }
        });
    }

    public Boolean metricMightBeMapped(final MetricDefinition metricDefinition) {
        if (!detectorMappingFilterEnabled) {
            log.debug("Metric {} not filtered as filter is disabled.", metricDefinition);
            return true;
        }
        mappingFilterMightMatchTotalCounter.inc();
        final String metricKey = CacheUtil.getKey(metricDefinition.getTags().getKv());
        final Boolean metricMightBeMappedResult = this.detectorMappingBloomFilter.mightContain(metricKey);
        if (metricMightBeMappedResult) {
            mappingFilterMightMatchPositiveCounter.inc();
        } else {
            mappingFilterMightMatchNegativeCounter.inc();
        }
        log.debug("Metric {} mightBeMapped={}", metricDefinition, metricMightBeMappedResult);
        return metricMightBeMappedResult;
    }

}
