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
package com.expedia.adaptivealerting.anomdetect;


import com.expedia.adaptivealerting.anomdetect.comp.DetectorSource;
import com.expedia.adaptivealerting.anomdetect.mapper.CacheUtil;
import com.expedia.adaptivealerting.anomdetect.mapper.Detector;
import com.expedia.adaptivealerting.anomdetect.mapper.DetectorMapping;
import com.expedia.adaptivealerting.anomdetect.mapper.DetectorMatchResponse;
import com.expedia.adaptivealerting.anomdetect.mapper.es.ESMatchingDetectorsResponse;
import com.expedia.adaptivealerting.anomdetect.mapper.es.ExpressionTree;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.metrics.MetricData;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Entry into the Adaptive Alerting runtime. Its job is find for any incoming {@link MetricData} the corresponding set
 * of mapped detectors, creating a {@link MappedMetricData} for each.
 */
@Slf4j
public class DetectorMapper {

    private Counter cacheHit;
    private Counter cacheMiss;
    private AtomicLong cacheSize;
    private AtomicLong indexSize;
    private Cache<String, String> cache;
    //TODO - need to handle this better instead of using a variable
    private long lastElasticLookUpLatency = -1;
    @Getter
    @NonNull
    private DetectorSource detectorSource;


    public DetectorMapper(DetectorSource detectorSource) {

        assert detectorSource!=null;

        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(120, TimeUnit.MINUTES)
                .build();

        this.detectorSource = detectorSource;
        this.cacheSize = Metrics.gauge("cache.size", new AtomicLong(0));
        this.indexSize = Metrics.gauge("index.size", new AtomicLong(0));
        this.cacheHit = Metrics.counter("cache.hit");
        this.cacheMiss = Metrics.counter("cache.miss");
    }

    public List<Detector> getDetectorsFromCache(MetricData metricData) {

        List<Detector> detectors = new ArrayList<>();

        String cacheKey = CacheUtil.getKey(metricData.getMetricDefinition().getTags().getKv());
        String cachedDetectorIdsString = cache.getIfPresent(cacheKey);

        if (cachedDetectorIdsString == null) {
            this.cacheMiss.increment();
        } else {
            this.cacheHit.increment();
            detectors = CacheUtil.buildDetectors(cachedDetectorIdsString);
        }

        return detectors;

    }

    /**
     * Maps an {@link MetricData} to its corresponding set of {@link MappedMetricData}s.
     *
     * @param metricData MetricData to map.
     * @return The corresponding set of {@link MappedMetricData}s: one per detector.
     */
    @Deprecated
    public Set<MappedMetricData> map(MetricData metricData) {
        notNull(metricData, "metricData can't be null");
        return detectorSource
                .findDetectorUuids(metricData.getMetricDefinition())
                .stream()
                .map(detectorUuid -> new MappedMetricData(metricData, detectorUuid))
                .collect(Collectors.toSet());
    }

    public boolean fetchDetectorMapping(List<Map<String, String>> cacheMissedMetricTags) {

        boolean isSuccessFull;
        log.info("Mapping-Cache: lookup for {} metrics", cacheMissedMetricTags.size());
        ESMatchingDetectorsResponse matchingDetectorMappings = detectorSource.findMatchingDetectorMappings(cacheMissedMetricTags);

        if (matchingDetectorMappings != null) {

            DetectorMatchResponse response = process(matchingDetectorMappings);

            lastElasticLookUpLatency = response.getLookupTimeInMillis();
            Map<Integer, List<Detector>> groupedDetectorsByIndex = response.getGroupedDetectorsByIndex();

            //populate cache and result map
            groupedDetectorsByIndex.forEach((index, detectors) -> {
                String cacheKey = CacheUtil.getKey(cacheMissedMetricTags.get(index));
                if (!detectors.isEmpty()) {
                    String detectorIdsString = CacheUtil.getDetectorIdsString(detectors);
                    log.info("Updating cache with {} - {}", cacheKey, detectorIdsString);
                    cache.put(cacheKey, detectorIdsString);
                }
            });

            Set<Integer> searchIndexes = groupedDetectorsByIndex.keySet();
            indexSize.set(searchIndexes.size());

//For metrics with no matching detectors, set matching detectors to empty in cache to avoid repeated cache miss
            final AtomicInteger i = new AtomicInteger(0);
            cacheMissedMetricTags.forEach(tags -> {
                if (!searchIndexes.contains(i.get())) {
                    String cacheKey = CacheUtil.getKey(tags);
                    cache.put(cacheKey, "");
                }
                i.incrementAndGet();
            });

            isSuccessFull = true;
        } else {
            lastElasticLookUpLatency = -2;
            isSuccessFull = false;
        }

        this.cacheSize.set(cache.size());
        return isSuccessFull;
    }

    //TODO move this to modelService  DetectorMatchResponse
    private DetectorMatchResponse process(ESMatchingDetectorsResponse res) {
        Map<Integer, List<Detector>> groupedDetectorsByIndex = new HashMap<>();
        log.info("Mapping-Cache: found {} matching mappings", res.getDetectorMappings().size());
        res.getDetectorMappings().forEach(detectorMapping -> {
            detectorMapping.getSearchIndexes().forEach(searchIndex -> {
                groupedDetectorsByIndex.computeIfAbsent(searchIndex, index -> new ArrayList<>());
                groupedDetectorsByIndex.computeIfPresent(searchIndex, (index, list) -> {
                    list.add(detectorMapping.getDetector());
                    return list;
                });
            });

        });
        return new DetectorMatchResponse(groupedDetectorsByIndex, res.getLookupTimeInMillis());
    }

    //TODO - need to improve this
    public int optimalBatchSize() {
        if (lastElasticLookUpLatency == -1 || lastElasticLookUpLatency > 100) {
            return 80;
        }
        return 0;
    }

    public void removeFromCache(List<DetectorMapping> disabledDetectorMappings) {
        Map<String, String> modifiedDetectorMappings = new HashMap<>();

        Map<String, String> mappingsWhichNeedsAnUpdate = new HashMap<>();

        this.cache.asMap().entrySet().forEach(mapping -> {
            disabledDetectorMappings.forEach(disabledDetectorMapping -> {
                if (mapping.getValue().contains(disabledDetectorMapping.getDetector().getId().toString())) {
                    mappingsWhichNeedsAnUpdate.put(mapping.getKey(), mapping.getValue());
                }
            });
        });

        mappingsWhichNeedsAnUpdate.entrySet().forEach(i -> {
            mappingsWhichNeedsAnUpdate.entrySet().forEach(mapping -> {
                String bunchOfUpdatedDetectorIds =
                        removeDisabledDetectorIds(getDetectorIds(disabledDetectorMappings), mapping.getValue());
                modifiedDetectorMappings.put(mapping.getKey(), bunchOfUpdatedDetectorIds);
            });
        });

        log.info("removing mappings : {} from cache entries",
                Arrays.toString(getDetectorIds(disabledDetectorMappings).toArray()));
        modifiedDetectorMappings.entrySet().forEach(modifiedDetectorMapping -> {
            log.info("cache key: {}, updated mapping {}", modifiedDetectorMapping.getKey(), modifiedDetectorMapping.getValue());
        });

        this.cache.putAll(modifiedDetectorMappings);
    }

    private List<UUID> getDetectorIds(List<DetectorMapping> disabledDetectorMappings) {
        return disabledDetectorMappings
                .stream().map(detectorMapping -> detectorMapping.getDetector().getId())
                .collect(Collectors.toList());
    }

    private String removeDisabledDetectorIds(List<UUID> detectorUuids, String detectorIdsString) {
        List<Detector> detectors = CacheUtil.buildDetectors(detectorIdsString);
        detectorUuids.forEach(uuid -> {
            detectors.remove(new Detector(uuid));
        });
        return CacheUtil.getDetectorIdsString(detectors);
    }

    public void updateCache(List<DetectorMapping> newDetectorMappings) {
        final List<String> matchingMappings = new ArrayList<>();
        List<Map<String, String>> listOfTagsFromExpression = findTags(newDetectorMappings);

        //iterate over the list of cache entries and find for matches and invalidate those from cache.
        //FIXME - This is a brute force approach with time complexity of O(n * m).
        // But assumption is that this will work as we are doing this in memory
        // and m (no of new mappings) will be always less.
        this.cache.asMap().entrySet().forEach(mapping -> {
            Map<String, String> metricTags = CacheUtil.getTags(mapping.getKey());
            if (doMetricTagsMatchesWithTagsPresentInExpression(metricTags, listOfTagsFromExpression)) {
                matchingMappings.add(mapping.getKey());
            }
        });
        log.info("invalidating cache entries: {} for input : {}",
                Arrays.toString(matchingMappings.toArray()),
                Arrays.toString(newDetectorMappings.stream()
                        .map(mapping -> mapping.getDetector().getId().toString())
                        .collect(Collectors.toList()).toArray()));
        //invalidate matches.
        cache.invalidateAll(matchingMappings);
    }

    private List<Map<String, String>> findTags(List<DetectorMapping> newDetectorMappings) {
        return newDetectorMappings.stream()
                .map(detectorMapping ->
                        findTagsFromDetectorMappingExpression(detectorMapping.getConditionExpression()))
                .collect(Collectors.toList());
    }

    private boolean doMetricTagsMatchesWithTagsPresentInExpression(Map<String, String> metricTags,
                                                                   List<Map<String, String>> tagsFromDetectorMappingExpression) {
        //FIXME - we are doing an exact match here. so this will work as along as we always use AND condition
        //in expression.
        //we need to improve this logic to handle OR, NOT conditions as well.
        for (Map<String, String> tags : tagsFromDetectorMappingExpression) {
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                if (metricTags.get(entry.getKey()) == null
                        || !metricTags.get(entry.getKey()).equals(entry.getValue())) {
                    return false;
                }
            }
        }
        return true;
    }

    private Map<String, String> findTagsFromDetectorMappingExpression(ExpressionTree expression) {
        return expression.getOperands()
                .stream().collect(Collectors.toMap(op -> op.getField().getKey(), op -> op.getField().getValue()));
    }


}

