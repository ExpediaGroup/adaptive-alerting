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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * <p>Cache for metric to detector-uuid mapping backed by Guava cache
 *
 *     {@code Map[key: metric-key, value: concatenated detectorUUIds] }<br>
 *
 * metric is identified by metric-key generated using {@link CacheUtil#getKey(Map)} <br>
 * detector's list is stored as concatenated string of detector uuid generated using {@link CacheUtil#getDetectorIds(List)}
 * <pre>
 * eg. if <em>
 *     Metric1
 *        {
 *          tag : {
 *                  k1:v1,
 *                  k2,v2
*                 }
 *        }</em>
 *
 *   has two matching detectors <em> D1(uuid= UUID_ONE), D2(uuid= UUID_TWO) </em>
 *   it will be stored in cache as <em> "k1:v1,k2:v2" : "UUID_ONE,UUID_TWO" </em>
 * </pre>
 *
 *  The DetectorMapperCache can be updated using methods {@link #removeDisabledDetectorMappings(List)} and {@link #updateCache(List)} }
 * <pre>
 * eg. if detector D1(uuid=UUID_ONE) is disabled, we need to remove it from all the values in cache.
 *
 *    <em> "k1:v1,k2:v2" : "UUID_ONE,UUID_TWO"</em>  changes to
 *    <em>"k1:v1,k2:v2" : "UUID_TWO"</em>
 * </pre>
 */
@Slf4j
public class DetectorMapperCache {

    private Cache<String, String> cache;
    private Counter cacheHit;
    private Counter cacheMiss;
    private AtomicLong cacheSize;

    /**
     * Instantiates a new Detector mapper cache.
     */
    public DetectorMapperCache() {
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(120, TimeUnit.MINUTES)
                .build();
        this.cacheSize = Metrics.gauge("cache.size", new AtomicLong(0));
        this.cacheHit = Metrics.counter("cache.hit");
        this.cacheMiss = Metrics.counter("cache.miss");
    }

    /**
     * @param key the metric-key generated using {@link CacheUtil#getKey(Map)} <br>
     * @return the list of Detectors
     */
    public List<Detector> get(String key) {
        String bunchOfCachedDetectorIds = cache.getIfPresent(key);
        if (bunchOfCachedDetectorIds == null) {
            this.cacheMiss.increment();
            return Collections.emptyList();
        } else {
            this.cacheHit.increment();
            return CacheUtil.buildDetectors(bunchOfCachedDetectorIds);
        }
    }

    /**
     * @param key       the metric-key generated using {@link CacheUtil#getKey(Map)} <br>
     * @param detectors the detectors
     */
    public void put(String key, List<Detector> detectors) {
        String bunchOfDetectorIds = CacheUtil.getDetectorIds(detectors);
        log.trace("Updating cache with {} - {}", key, bunchOfDetectorIds);
        cache.put(key, bunchOfDetectorIds);
        this.cacheSize.set(cache.size());
    }


    /**
     * Remove disabled detector mappings from cache.
     *
     * @param disabledMappings the list of mappings
     */
    public void removeDisabledDetectorMappings(List<DetectorMapping> disabledMappings) {
        List<UUID> detectorIdsOfDisabledMappings = disabledMappings.stream().map(detectorMapping -> detectorMapping.getDetector().getUuid()).collect(Collectors.toList());

        Map<String, String> mappingsWhichNeedsAnUpdate = new HashMap<>();

        this.cache.asMap().forEach((key, bunchOfDetectorIds) -> {
            if (detectorIdsOfDisabledMappings.stream().anyMatch(uuid -> bunchOfDetectorIds.contains(uuid.toString()))) {
                mappingsWhichNeedsAnUpdate.put(key, bunchOfDetectorIds);
            }
        });


        Map<String, String> modifiedDetectorMappings = new HashMap<>();
        mappingsWhichNeedsAnUpdate.forEach((key, bunchOfDetectorIds) -> {
            String bunchOfUpdatedDetectorIds =
                    removeDisabledDetectorIds(detectorIdsOfDisabledMappings, bunchOfDetectorIds);
            modifiedDetectorMappings.put(key, bunchOfUpdatedDetectorIds);
        });

        log.info("removing mappings : {} from cache entries",
                Arrays.toString(detectorIdsOfDisabledMappings.toArray()));
        modifiedDetectorMappings.forEach((key, value) -> log.info("cache key: {}, updated mapping {}", key, value));

        this.cache.putAll(modifiedDetectorMappings);
    }

    private String removeDisabledDetectorIds(List<UUID> detectorUuids, String detectorIdsString) {
        List<Detector> detectors = CacheUtil.buildDetectors(detectorIdsString);
        detectorUuids.forEach(uuid -> {
            detectors.remove(new Detector(uuid));
        });
        return CacheUtil.getDetectorIds(detectors);
    }

    /**
     * When a detector is updated, old mapping may still not be valid as metrics' tags might not match with new mapping expression
     * So this method removes metrics from cache which were previously  detectors the mappings that have been updated.
     *
     * This causes a cache-miss for removed metrics, and then eventually are re-populated by a call to backend
     *
     * @param newDetectorMappings the new detector mappings
     */
    public void updateCache(List<DetectorMapping> newDetectorMappings) {
        final List<String> matchingMappings = new ArrayList<>();
        List<Map<String, String>> listOfTagsFromExpression = findTags(newDetectorMappings);

        //iterate over the list of cache entries and find for matches and invalidate those from cache.
        //FIXME - This is a brute force approach with time complexity of O(n * m).
        // But assumption is that this will work as we are doing this in memory
        // and m (no of new mappings) will be always less.
        this.cache.asMap().forEach((metricKey, value) -> {
            Map<String, String> metricTags = CacheUtil.getTags(metricKey);
            if (doMetricTagsMatchesWithTagsPresentInExpression(metricTags, listOfTagsFromExpression)) {
                matchingMappings.add(metricKey);
            }
        });
        log.info("invalidating cache entries: {} for input : {}",
                Arrays.toString(matchingMappings.toArray()),
                Arrays.toString(newDetectorMappings.stream()
                        .map(mapping -> mapping.getDetector().getUuid().toString())
                        .toArray()));
        //invalidate matches.
        cache.invalidateAll(matchingMappings);
    }

    private List<Map<String, String>> findTags(List<DetectorMapping> newDetectorMappings) {
        return newDetectorMappings.stream()
                .map(detectorMapping ->
                        findTagsFromDetectorMappingExpression(detectorMapping.getExpression()))
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
