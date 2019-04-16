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
import com.expedia.adaptivealerting.anomdetect.mapper.DetectorMatchResponse;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.metrics.MetricData;
import io.micrometer.core.instrument.Metrics;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Entry into the Adaptive Alerting runtime. Its job is find for any incoming {@link MetricData} the corresponding set
 * of mapped detectors, creating a {@link MappedMetricData} for each.
 */
@Slf4j
public class DetectorMapper {
    private AtomicLong indexSize;
    private DetectorMapperCache cache;
    private AtomicLong lastElasticLookUpLatency = new AtomicLong(-1);
    @Getter
    @NonNull
    private DetectorSource detectorSource;


    public DetectorMapper(DetectorSource detectorSource) {
        assert detectorSource!=null;
        this.detectorSource = detectorSource;
        this.indexSize = Metrics.gauge("index.size", new AtomicLong(0));
        this.cache = new DetectorMapperCache();
    }

    public List<Detector> getDetectorsFromCache(MetricData metricData) {
        String cacheKey = CacheUtil.getKey(metricData.getMetricDefinition().getTags().getKv());
        return cache.get(cacheKey);
    }

    public boolean isSuccessfulDetectorMappingLookup(List<Map<String, String>> cacheMissedMetricTags) {

        log.info("Mapping-Cache: lookup for {} metrics", cacheMissedMetricTags.size());
        DetectorMatchResponse matchingDetectorMappings = detectorSource.findMatchingDetectorMappings(cacheMissedMetricTags);

        if (matchingDetectorMappings != null) {

            lastElasticLookUpLatency.set(matchingDetectorMappings.getLookupTimeInMillis());
            Map<Integer, List<Detector>> groupedDetectorsByIndex = matchingDetectorMappings.getGroupedDetectorsBySearchIndex();

            //populate cache and result map
            groupedDetectorsByIndex.forEach((index, detectors) -> {
                String cacheKey = CacheUtil.getKey(cacheMissedMetricTags.get(index));
                if (!detectors.isEmpty()) {
                    cache.put(cacheKey, detectors);
                }
            });

            Set<Integer> searchIndexes = groupedDetectorsByIndex.keySet();
            indexSize.set(searchIndexes.size());

//For metrics with no matching detectors, set matching detectors to empty in cache to avoid repeated cache miss
            final AtomicInteger i = new AtomicInteger(0);
            cacheMissedMetricTags.forEach(tags -> {
                if (!searchIndexes.contains(i.get())) {
                    String cacheKey = CacheUtil.getKey(tags);
                    cache.put(cacheKey, Collections.EMPTY_LIST);
                }
                i.incrementAndGet();
            });

        } else {
            lastElasticLookUpLatency.set(-2);
        }
        return matchingDetectorMappings != null;
    }

    //TODO - make batch size configureable
    public int optimalBatchSize() {
        if (lastElasticLookUpLatency.longValue() == -1L || lastElasticLookUpLatency.longValue() > 100L) {
            return 80;
        }
        return 0;
    }

}

