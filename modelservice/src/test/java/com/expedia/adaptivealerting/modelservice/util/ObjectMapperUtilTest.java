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
