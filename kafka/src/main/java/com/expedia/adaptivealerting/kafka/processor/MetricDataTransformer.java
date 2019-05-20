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
package com.expedia.adaptivealerting.kafka.processor;

import com.expedia.adaptivealerting.anomdetect.detectormapper.Detector;
import com.expedia.adaptivealerting.anomdetect.detectormapper.DetectorMapper;
import com.expedia.adaptivealerting.anomdetect.detectormapper.MapperResult;
import com.expedia.metrics.MetricData;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A custom stateful KStream transformer that converts {@link MetricData} to {@link MapperResult}.
 * For each incoming record, {@link #transform(String key, MetricData metricData) , matching detectors are fetched from cache
 * in case of cache miss, record in pushed into a in-memory state store, for batching.
 * <p>
 * {@link #init(ProcessorContext context), registers a scheduled a periodic operation that determines if the batch size is appropriate
 * and issues a down stream call to fetch matching detectors.
 * <p>
 * <p>
 * While pushing records into state store, using {@code key} can cause overriding metric of same {@code  metricDefinition} as state store is a Map.
 * Hence we use {@link #addSalt(String key)} method while inserting the record and {@link #removeSalt(String key)} while pushing result.
 * Thus we have same key through out transformation which prevents data re-partitioning.
 * <p>
 * Note: Since we want to preserve key, using ValueTransformerWithKey might seem the right choice but it doesn't allow pushing key value pair using {@link #context.forward()}
 * https://docs.confluent.io/current/streams/javadocs/org/apache/kafka/streams/kstream/KStream.html#transformValues-org.apache.kafka.streams.kstream.ValueTransformerSupplier-java.lang.String...-
 */
@Slf4j
@Data
@RequiredArgsConstructor
class MetricDataTransformer implements Transformer<String, MetricData, KeyValue<String, MapperResult>> {

    private ProcessorContext context;
    private KeyValueStore<String, MetricData> metricDataKeyValueStore;

    @NonNull
    private DetectorMapper detectorMapper;
    @NonNull
    private final String stateStoreName;


    private String addSalt(String key) {
        return key.concat(":").concat(UUID.randomUUID().toString());
    }

    private String removeSalt(String key) {
        if (key.contains(":"))
            return key.substring(0, key.indexOf(":"));
        else
            return key;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void init(ProcessorContext context) {
        this.context = context;
        this.metricDataKeyValueStore = (KeyValueStore<String, MetricData>) context.getStateStore(stateStoreName);

        //TODO decide PUNCTUATION time
        this.context.schedule(200, PunctuationType.WALL_CLOCK_TIME, (timestamp) -> {

            if (metricDataKeyValueStore.approximateNumEntries() >= detectorMapper.optimalBatchSize()) {
                KeyValueIterator<String, MetricData> iter = this.metricDataKeyValueStore.all();
                Map<String, MetricData> cacheMissedMetrics = new HashMap<>();

                while (iter.hasNext()) {
                    KeyValue<String, MetricData> entry = iter.next();
                    cacheMissedMetrics.put(entry.key, entry.value);
                    metricDataKeyValueStore.delete(entry.key);
                }
                iter.close();

                List<Map<String, String>> cacheMissedMetricTags = cacheMissedMetrics.values().stream().map(value -> value.getMetricDefinition().getTags().getKv()).collect(Collectors.toList());
                if (!cacheMissedMetricTags.isEmpty() && detectorMapper.isSuccessfulDetectorMappingLookup(cacheMissedMetricTags)) {
                    cacheMissedMetrics.forEach((originalKey, metricData) -> {
                        List<Detector> detectors = detectorMapper.getDetectorsFromCache(metricData.getMetricDefinition());
                        if (!detectors.isEmpty()) {
                            context.forward(removeSalt(originalKey), new MapperResult(metricData, detectors));
                        }
                    });
                }

            } else {
                log.trace("ES lookup skipped, as batch size is not optimum");
            }

            // commit the current processing progress
            context.commit();
        });

    }

    @Override
    public KeyValue<String, MapperResult> transform(String key, MetricData metricData) {

        List<Detector> detectors = detectorMapper.getDetectorsFromCache(metricData.getMetricDefinition());

        if (detectors.isEmpty()) {
            //adding salt to key to prevent incoming records with same key being over-ridden
            this.metricDataKeyValueStore.put(addSalt(key), metricData);
        } else {
            return new KeyValue<>(key, new MapperResult(metricData, detectors));
        }
        return null;

    }

    @Override
    public KeyValue<String, MapperResult> punctuate(long timestamp) {
        return null;
    }


    @Override
    public void close() {
    }
}
