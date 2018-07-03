/*
 * Copyright 2018 Expedia Group, Inc.
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

import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

/**
 * Timestamp extractor for MetricPoints
 * similar to class {@link org.apache.kafka.streams.processor.LogAndSkipOnInvalidTimestamp}
 * adding this as com.expedia.www.haystack.commons.kstreams.MetricPointTimestampExtractor doesn't handle this
 */
public class AnomalyResultTimeStampExtractor implements TimestampExtractor {

    @Override
    public long extract(ConsumerRecord<Object, Object> record, long previousTimestamp) {
        if (record == null || record.value() == null || !(record.value() instanceof AnomalyResult)) {
            // Returns -1 as timestamp which ultimately causes the record to be skipped and not to be processed
            return -1;
        }
        Long epochSecond = ((AnomalyResult) record.value()).getEpochSecond();
        return epochSecond == null ? -1 : epochSecond * 1000;
    }
}
