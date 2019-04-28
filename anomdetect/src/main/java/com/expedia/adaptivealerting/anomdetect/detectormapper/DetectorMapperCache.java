package com.expedia.adaptivealerting.anomdetect.detectormapper;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

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
        String bunchOfCachedDetectorIds = cache.getIfPresent(key);
        if (bunchOfCachedDetectorIds == null) {
            this.cacheMiss.increment();
            return Collections.emptyList();
        } else {
            this.cacheHit.increment();
            return CacheUtil.buildDetectors(bunchOfCachedDetectorIds);
        }
    }

    public void put(String key, List<Detector> detectors) {
        String bunchOfDetectorIds = CacheUtil.getDetectorIds(detectors);
        log.trace("Updating cache with {} - {}", key, bunchOfDetectorIds);
        cache.put(key, bunchOfDetectorIds);
        this.cacheSize.set(cache.size());
    }
}
