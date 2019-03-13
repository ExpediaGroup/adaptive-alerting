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

import com.expedia.adaptivealerting.kafka.util.TestObjectMother;
import com.expedia.alertmanager.model.Alert;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class AlertJsonSerializerTest {
    private AlertJsonSerializer serializerUnderTest;
    private ObjectMapper objectMapper;
    private Alert alert;

    @Before
    public void setUp() {
        this.serializerUnderTest = new AlertJsonSerializer();
        this.objectMapper = new ObjectMapper();
        this.alert = TestObjectMother.alert();
    }

    @Test
    public void testSerialize() throws Exception {
        val expected = objectMapper.writeValueAsBytes(alert);
        val actual = serializerUnderTest.serialize("some-topic", alert);
        assertArrayEquals(expected, actual);
    }
}
