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
import com.expedia.adaptivealerting.anomdetect.mapper.es.ExpressionTree;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.metrics.MetricData;
import io.micrometer.core.instrument.Metrics;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
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
        if (lastElasticLookUpLatency.longValue() == -1 || lastElasticLookUpLatency.longValue() > 100) {
            return 80;
        }
        return 0;
    }

    public void removeMappings(List<DetectorMapping> disabledDetectorMappings) {
        List<Detector> detectors = disabledDetectorMappings.stream()
                .map(disabledDetectorMapping -> disabledDetectorMapping.getDetector())
                .collect(Collectors.toList());
        this.cache.removeFromCache(detectors);
    }

    public void addMappings(List<DetectorMapping> newDetectorMappings) {
        final List<String> matchingMappings = new ArrayList<>();
        List<Map<String, String>> listOfGroupOfTagsFromExpression = findTags(newDetectorMappings);
        //invalidating the matching keys so that cache will be refilled
        //from mappings store whenever any new metrics comes
        cache.invalidateKeysMatchingTags(listOfGroupOfTagsFromExpression);
    }

    private List<Map<String, String>> findTags(List<DetectorMapping> newDetectorMappings) {
        return newDetectorMappings.stream()
                .map(detectorMapping ->
                        findTagsFromDetectorMappingExpression(detectorMapping.getExpression()))
                .collect(Collectors.toList());
    }

    private Map<String, String> findTagsFromDetectorMappingExpression(ExpressionTree expression) {
        return expression.getOperands()
                .stream().collect(Collectors.toMap(op -> op.getField().getKey(), op -> op.getField().getValue()));
    }


}

