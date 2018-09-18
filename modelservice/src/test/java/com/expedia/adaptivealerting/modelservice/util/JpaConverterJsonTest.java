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
package com.expedia.adaptivealerting.modelservice.util;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author kashah
 */
public class JpaConverterJsonTest {

    /* Class under test */
    @InjectMocks
    private JpaConverterJson convertor;

    @Mock
    private ObjectMapper mapper;

    private String expectedString;
    private Map expectedObject;

    @Before
    public void setUp() throws Exception {
        this.convertor = new JpaConverterJson();
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
    }

    @Test
    public void testConvertToEntityAttribute() {
        Object actualObject = convertor.convertToEntityAttribute(expectedString);
        assertEquals(expectedObject, actualObject);
    }

    @Test
    public void testConvertToDatabaseColumn() {
        Object actualString = convertor.convertToDatabaseColumn(expectedObject);
        assertEquals(expectedString, actualString);
    }

    private void initTestObjects() {

        this.expectedObject = new LinkedHashMap<>();
        expectedObject.put("test1", 1);
        expectedObject.put("test2", 2);
        expectedObject.put("test3", 3);

        this.expectedString = "{\"test1\":1,\"test2\":2,\"test3\":3}";
    }

    private void initDependencies() {
        ObjectMapper mockObjectMapper = Mockito.mock(ObjectMapper.class);

        try {
            Mockito.when(mapper.writeValueAsString(expectedObject)).thenReturn("");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Mockito.when(mapper.readValue("", Map.class)).thenReturn(expectedObject);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
