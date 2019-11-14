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
package com.expedia.adaptivealerting.modelservice.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ObjectMapperUtilTest {
    private static final TypeReference<Map> MAP_TYPE_REFERENCE = new TypeReference<Map>() {};

    private ObjectMapperUtil utilUnderTest;

    @Before
    public void setUp() {
        this.utilUnderTest = new ObjectMapperUtil();
    }

    @Test
    public void testConvertToString() throws JsonProcessingException {
        val someObj = Collections.singletonMap("foo", "bar");
        val expected = new ObjectMapper().writeValueAsString(someObj);
        val actual = utilUnderTest.convertToString(someObj);
        assertEquals(expected, actual);
    }

    @Test(expected = RuntimeException.class)
    public void testConvertToString_invalidObject() {
        utilUnderTest.convertToString(new Object());
    }

    @Test
    public void testConvertToObject() throws JsonProcessingException {
        val someJson = "{ \"foo\" : \"bar\" }";
        val expected = new ObjectMapper().readValue(someJson, MAP_TYPE_REFERENCE);
        val actual = utilUnderTest.convertToObject(someJson, MAP_TYPE_REFERENCE);
        assertEquals(expected, actual);
    }

    @Test(expected = RuntimeException.class)
    public void testConvertToObject_invalidJson() {
        utilUnderTest.convertToObject("", MAP_TYPE_REFERENCE);
    }
}
