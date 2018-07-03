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

import com.expedia.adaptivealerting.core.data.Mpoint;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Timestamp extractor for {@link Mpoint}, similar to
 * {@link org.apache.kafka.streams.processor.LogAndSkipOnInvalidTimestamp}.
 * Adding this as com.expedia.www.haystack.commons.kstreams.MetricPointTimestampExtractor doesn't handle null values.
 *
 * @author Shubham Sethi
 * @author Willie Wheeler
 */
public class MpointTimestampExtractor implements TimestampExtractor {
    private static final Logger log = LoggerFactory.getLogger(MpointTimestampExtractor.class);
    
    @Override
    public long extract(ConsumerRecord<Object, Object> record, long previousTimestamp) {
        Mpoint mpoint = (Mpoint) record.value();
        if (mpoint == null) {
            // -1 skips the record.
            log.warn("Skipping null Mpoint");
            return -1L;
        }
        
        return mpoint.getEpochTimeInSeconds() * 1000L;
    }
}
