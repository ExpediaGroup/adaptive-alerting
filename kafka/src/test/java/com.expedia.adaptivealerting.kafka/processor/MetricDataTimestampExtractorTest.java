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

import com.expedia.adaptivealerting.kafka.util.TestObjectMother;
import lombok.val;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class MetricDataTimestampExtractorTest {

    @Test
    public void testExtract() {
        val metricData = TestObjectMother.metricData();
        val record = new ConsumerRecord<Object, Object>("some-topic", 1, 0, "some-key", metricData);
        val expectedTimestamp = metricData.getTimestamp() * 1000L;
        val actualTimestamp = new MetricDataTimestampExtractor().extract(record, -1L);
        assertEquals(expectedTimestamp, actualTimestamp);
    }

    @Test
    public void testExtract_nullMetricData() {
        val record = new ConsumerRecord<Object, Object>("some-topic", 1, 0, "some-key", null);
        val actualTimestamp = new MetricDataTimestampExtractor().extract(record, -1L);
        assertTrue(actualTimestamp < 0L);
    }
}
