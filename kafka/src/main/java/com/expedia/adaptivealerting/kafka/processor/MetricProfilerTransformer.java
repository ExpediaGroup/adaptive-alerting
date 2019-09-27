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

import com.expedia.adaptivealerting.metricprofiler.MetricProfiler;
import com.expedia.metrics.MetricData;
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
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class MetricProfilerTransformer implements Transformer<String, MetricData, KeyValue<String, MetricData>> {

    private ProcessorContext context;
    private KeyValueStore<String, MetricData> metricDataKeyValueStore;

    @NonNull
    private MetricProfiler metricProfiler;

    @NonNull
    private final String stateStoreName;

    @Override
    @SuppressWarnings("unchecked")
    public void init(ProcessorContext context) {
        this.context = context;
        this.metricDataKeyValueStore = (KeyValueStore<String, MetricData>) context.getStateStore(stateStoreName);

        /*
         *  Requests to model-service to fetch profiling info for metrics are throttled
         *  This is done by buffering them in kafka KV store and periodically clearing that buffer
         *
         */
        this.context.schedule(200, PunctuationType.WALL_CLOCK_TIME, (timestamp) -> {
            if (metricDataKeyValueStore.approximateNumEntries() >= metricProfiler.optimalBatchSize()) {
                Map<String, MetricData> metricsToBeProfiled = getMapAndFlushStore();
                metricsToBeProfiled.forEach((originalKey, metricData) -> {
                    Boolean profilingInfo = metricProfiler.hasProfilingInfo(metricData.getMetricDefinition());
                    if (profilingInfo == null) {
                        context.forward(originalKey, metricData);
                    }
                });
            } else {
                log.trace("ES lookup skipped, as batch size is not optimum");
            }
            context.commit();
        });

    }

    @Override
    public KeyValue<String, MetricData> transform(String key, MetricData metricData) {
        Boolean profilingInfo = metricProfiler.hasProfilingInfo(metricData.getMetricDefinition());
        if (profilingInfo == null) {
            return new KeyValue<>(key, metricData);
        }
        return null;
    }

    @Override
    public KeyValue<String, MetricData> punctuate(long timestamp) {
        return null;
    }


    @Override
    public void close() {
    }

    private Map<String, MetricData> getMapAndFlushStore() {
        KeyValueIterator<String, MetricData> iter = this.metricDataKeyValueStore.all();
        Map<String, MetricData> metricsToBeProfiled = new HashMap<>();

        while (iter.hasNext()) {
            KeyValue<String, MetricData> entry = iter.next();
            metricsToBeProfiled.put(entry.key, entry.value);
            metricDataKeyValueStore.delete(entry.key);
        }
        iter.close();
        return metricsToBeProfiled;
    }
}

