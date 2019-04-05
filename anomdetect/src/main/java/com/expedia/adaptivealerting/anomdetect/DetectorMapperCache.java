package com.expedia.adaptivealerting.anomdetect;

import com.expedia.adaptivealerting.anomdetect.mapper.CacheUtil;
import com.expedia.adaptivealerting.anomdetect.mapper.Detector;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
public class DetectorMapperCache {

    private Cache<String, String> cache;
    private Counter cacheHit;
    private Counter cacheMiss;
    private AtomicLong cacheSize;

    public DetectorMapperCache() {
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(120, TimeUnit.MINUTES)
                .build();
        this.cacheSize = Metrics.gauge("cache.size", new AtomicLong(0));
        this.cacheHit = Metrics.counter("cache.hit");
        this.cacheMiss = Metrics.counter("cache.miss");
    }

    public List<Detector> get(String key) {
        List<Detector> detectors = new ArrayList<>();
        String bunchOfCachedDetectorIds = cache.getIfPresent(key);
        if (bunchOfCachedDetectorIds == null) {
            this.cacheMiss.increment();
        } else {
            this.cacheHit.increment();
            detectors = CacheUtil.buildDetectors(bunchOfCachedDetectorIds);
        }
        return detectors;
    }

    public void put(String key, List<Detector> detectors) {
        String bunchOfDetectorIds = CacheUtil.getDetectorIds(detectors);
        log.info("Updating cache with {} - {}", key, bunchOfDetectorIds);
        cache.put(key, bunchOfDetectorIds);
        this.cacheSize.set(cache.size());
    }

    public void removeFromCache(List<Detector> detectors) {
        Map<String, String> modifiedDetectorMappings = new HashMap<>();
        Map<String, String> mappingsWhichNeedsAnUpdate = new HashMap<>();
        this.cache.asMap().entrySet().forEach(mapping -> {
            detectors.forEach(detector -> {
                if (mapping.getValue().contains(detector.getUuid().toString())) {
                    mappingsWhichNeedsAnUpdate.put(mapping.getKey(), mapping.getValue());
                }
            });
        });
        mappingsWhichNeedsAnUpdate.entrySet().forEach(i -> {
            mappingsWhichNeedsAnUpdate.entrySet().forEach(mapping -> {
                String bunchOfUpdatedDetectorIds =
                        removeDisabledDetectorIds(getDetectorIds(detectors), mapping.getValue());
                modifiedDetectorMappings.put(mapping.getKey(), bunchOfUpdatedDetectorIds);
            });
        });
        log.info("removing mappings : {} from cache entries",
                Arrays.toString(getDetectorIds(detectors).toArray()));
        modifiedDetectorMappings.entrySet().forEach(modifiedDetectorMapping ->
            log.info("cache key: {}, updated mapping {}", modifiedDetectorMapping.getKey(),
                    modifiedDetectorMapping.getValue())
        );

        this.cache.putAll(modifiedDetectorMappings);
    }

    private String removeDisabledDetectorIds(List<UUID> detectorUuids, String bunchOfDetectorIds) {
        List<Detector> detectors = CacheUtil.buildDetectors(bunchOfDetectorIds);
        detectorUuids.forEach(uuid -> {
            detectors.remove(new Detector(uuid));
        });
        return CacheUtil.getDetectorIds(detectors);
    }

    private List<UUID> getDetectorIds(List<Detector> detectors) {
        return detectors
                .stream().map(detector -> detector.getUuid())
                .collect(Collectors.toList());
    }

    public void invalidateKeysMatchingTags(List<Map<String, String>> listOfSetOfTags) {
        final List<String> matchingMappings = new ArrayList<>();
        //iterate over the list of cache entries and find for matches and invalidate those from cache.
        //FIXME - This is a brute force approach with time complexity of O(n * m).
        // But assumption is that this will work as we are doing this in memory
        // and m (no of new mappings) will be always less.
        this.cache.asMap().entrySet().forEach(mapping -> {
            Map<String, String> metricTags = CacheUtil.getTags(mapping.getKey());
            if (isAnyGroupIsASubsetOfMetricTags(metricTags, listOfSetOfTags)) {
                matchingMappings.add(mapping.getKey());
            }
        });
        //invalidate matches.
        cache.invalidateAll(matchingMappings);
        this.cacheSize.set(cache.size());
    }

    private boolean isAnyGroupIsASubsetOfMetricTags(Map<String, String> metricTags,
                                                    List<Map<String, String>> listOfGroupOfTags) {
        //FIXME - we are doing an exact match here. so this will work as along as we always use AND condition
        //in expression.
        //we need to improve this logic to handle OR, NOT conditions as well.
        for (Map<String, String> tags : listOfGroupOfTags) {
            if (isFirstSubsetOfSecond(tags, metricTags)) return true;
        }
        return false;
    }

    private boolean isFirstSubsetOfSecond(Map<String, String> firstSet, Map<String, String> secondSet) {
        for (Map.Entry<String, String> entry : firstSet.entrySet()) {
            if (secondSet.get(entry.getKey()) == null
                    || !secondSet.get(entry.getKey()).equals(entry.getValue())) {
                return true;
            }
        }
        return false;
    }
}
