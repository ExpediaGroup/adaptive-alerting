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

import com.expedia.adaptivealerting.anomdetect.detector.MappedMetricData;
import lombok.val;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

/**
 * Extracts a timestamp from a {@link MappedMetricData} consumer record.
 */
public final class MappedMetricDataTimestampExtractor implements TimestampExtractor {

    @Override
    public long extract(ConsumerRecord<Object, Object> record, long previousTimestamp) {
        val mappedMetricData = (MappedMetricData) record.value();
        if (mappedMetricData == null || mappedMetricData.getMetricData() == null) {
            // -1 skips the record. Don't log as it can fill up the logs.
            return -1L;
        }
        return mappedMetricData.getMetricData().getTimestamp() * 1000L;
    }
}
