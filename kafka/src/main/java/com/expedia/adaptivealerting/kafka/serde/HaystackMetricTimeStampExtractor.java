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

import com.expedia.www.haystack.commons.entities.MetricPoint;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

/**
 * Timestamp extractor for MetricPoints
 * similar to class {@link org.apache.kafka.streams.processor.LogAndSkipOnInvalidTimestamp}
 * adding this as com.expedia.www.haystack.commons.kstreams.MetricPointTimestampExtractor doesn't handle this

 * @author shsethi
 */
public class HaystackMetricTimeStampExtractor implements TimestampExtractor {

    @Override
    public long extract(ConsumerRecord<Object, Object> record, long previousTimestamp) {

        MetricPoint metricPoint = (MetricPoint) (record.value());

        if (metricPoint != null) {
            //The startTime for metricpoints in computed in seconds and hence multiplying by 1000 to create the epochTimeInMs
            return metricPoint.epochTimeInSeconds() * 1000;
        } else {
            // Returns -1 as timestamp which ultimately causes the record to be skipped and not to be processed
            return -1;
        }

    }
}

