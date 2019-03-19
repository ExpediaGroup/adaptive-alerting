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

import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.adaptivealerting.kafka.util.TestObjectMother;
import com.expedia.metrics.jackson.MetricsJavaModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

// TODO Use abstract base class for serde tests [WLW]
public final class MappedMetricDataJsonSerdeTest {
    private MappedMetricDataJsonSerde serdeUnderTest;
    private ObjectMapper objectMapper;
    private MappedMetricData mappedMetricData;

    @Before
    public void setUp() {
        this.serdeUnderTest = new MappedMetricDataJsonSerde();
        this.objectMapper = new ObjectMapper().registerModule(new MetricsJavaModule());
        this.mappedMetricData = TestObjectMother.mappedMetricData();
    }

    @Test
    public void coverageOnly() {
        serdeUnderTest.configure(null, false);
        serdeUnderTest.close();
    }

    @Test
    public void testSerialize() throws Exception {
        val expected = objectMapper.writeValueAsBytes(mappedMetricData);
        val actual = serdeUnderTest.serializer().serialize("some-topic", mappedMetricData);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testDeserialize() throws Exception {
        val expected = TestObjectMother.mappedMetricData();
        val expectedBytes = objectMapper.writeValueAsBytes(expected);
        val actual = serdeUnderTest.deserializer().deserialize("some-topic", expectedBytes);
        assertEquals(expected, actual);
    }
}
