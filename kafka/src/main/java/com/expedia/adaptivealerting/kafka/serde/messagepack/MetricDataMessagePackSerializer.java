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
package com.expedia.adaptivealerting.kafka.serde.messagepack;

import com.expedia.metrics.MetricData;
import com.expedia.metrics.metrictank.MessagePackSerializer;
import org.apache.kafka.common.serialization.Serializer;

import java.io.IOException;
import java.util.Map;

public class MetricDataMessagePackSerializer implements Serializer<MetricData> {
    private final static MessagePackSerializer mps = new MessagePackSerializer();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public byte[] serialize(String topic, MetricData metricData) {
        try {
            return mps.serialize(metricData);
        } catch (IOException e) {
            // FIXME This should be SerializationException. [WLW]
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
    }
}
