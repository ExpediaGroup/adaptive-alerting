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
package com.expedia.adaptivealerting.kafka.serde;

import com.expedia.metrics.MetricData;
import com.expedia.metrics.metrictank.MessagePackSerializer;
import com.expedia.metrics.metrictank.MetricTankMetricDefinition;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.io.IOException;
import java.util.Map;

/**
 * Serde implementation that enriches a {@link MetricData} with fields required by Metrictank, set to default values:
 * <ul>
 *     <li>orgId: 1</li>
 *     <li>mtype: gauge</li>
 *     <li>unit: [empty string]</li>
 *     <li>interval: 300</li>
 * </ul>
 */
@Slf4j
public class MetricTankMetricDataMessagePackSerde implements Serde<MetricData> {
    public static final int DEFAULT_ORG_ID = 1;

    /**
     * Default metric data interval, in seconds. Note that Metrictank will use this to snap timestamps, so this means
     * that Metrictank will aggregate (specifically, average) multiple metric data coming in at a shorter interval. In
     * other words, if we have metric data X0=100 and X1=200 coming in 30 seconds later, then Metrictank will interpret
     * this as a single metric data with value 150.
     */
    public static final int DEFAULT_INTERVAL = 60;

    public static final String DEFAULT_UNIT = "";
    public static final String DEFAULT_MTYPE = "gauge";

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public void close() {
    }

    @Override
    public Serializer<MetricData> serializer() {
        return new Ser();
    }

    @Override
    public Deserializer<MetricData> deserializer() {
        return new Deser();
    }

    public static class Ser implements Serializer<MetricData> {
        private static final MessagePackSerializer mps = new MessagePackSerializer();

        @Override
        public void configure(Map<String, ?> map, boolean b) {
        }

        @Override
        public byte[] serialize(String topic, MetricData metricData) {
            val metricTankMetricData = toMetricTankMetricData(metricData);
            try {
                return mps.serialize(metricTankMetricData);
            } catch (IOException e) {
                throw new SerializationException(e);
            }
        }

        @Override
        public void close() {
        }

        private MetricData toMetricTankMetricData(MetricData metricData) {
            val metricDef = metricData.getMetricDefinition();
            val mtMetricDef = new MetricTankMetricDefinition(
                    metricDef.getKey(),
                    metricDef.getTags(),
                    metricDef.getMeta(),
                    DEFAULT_ORG_ID,
                    DEFAULT_INTERVAL,
                    DEFAULT_UNIT,
                    DEFAULT_MTYPE);
            return new MetricData(mtMetricDef, metricData.getValue(), metricData.getTimestamp());
        }
    }

    public static class Deser implements Deserializer<MetricData> {
        private static final MessagePackSerializer mps = new MessagePackSerializer();

        @Override
        public void configure(Map<String, ?> map, boolean b) {
        }

        @Override
        public MetricData deserialize(String topic, byte[] metricDataBytes) {
            try {
                return mps.deserialize(metricDataBytes);
            } catch (IOException e) {
                log.error("Deserialization error", e);
                return null;
            }
        }

        @Override
        public void close() {
        }
    }
}
