package com.expedia.adaptivealerting.modelservice.util;

import com.expedia.adaptivealerting.modelservice.test.ObjectMother;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.val;
import org.hibernate.HibernateException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class ObjectMapperUtilTest {
    /* Class under test */
    @InjectMocks
    private ObjectMapperUtil objectMapperUtil;

    @Mock
    private ObjectMapper mapper;

    private String expectedString;
    private Map expectedObject;

    @Before
    public void setUp() throws Exception {
        this.objectMapperUtil = new ObjectMapperUtil();
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
    }

    @Test
    public void testConvertToString() {
        String actualString = objectMapperUtil.convertToString(new Object());
        assertEquals(expectedString, actualString);
    }


    @Test(expected = RuntimeException.class)
    public void testConvertToString_fail() throws JsonProcessingException {
        when(mapper.writeValueAsString(any())).thenThrow(new RuntimeException());
        String actualString = objectMapperUtil.convertToString(new Object());
    }

    @Test
    public void testConvertToObject() {
        Object actualObject = objectMapperUtil.convertToObject("", new TypeReference<Map>() {
        });
        assertEquals(expectedObject, actualObject);
    }

    @Test(expected = RuntimeException.class)
    public void testConvertToObject_fail() throws IOException {
        when(mapper.readValue(anyString(), any(TypeReference.class))).thenThrow(new RuntimeException());
        Object actualObject = objectMapperUtil.convertToObject("", new TypeReference<Map>() {
        });
    }

    private void initTestObjects() {
        val mom = ObjectMother.instance();
        expectedObject = mom.getTestObject();
        expectedString = mom.getTestString();
    }

    @SneakyThrows
    private void initDependencies() {
        when(mapper.writeValueAsString(any())).thenReturn(expectedString);
        when(mapper.readValue(anyString(), any(TypeReference.class))).thenReturn(expectedObject);
    }
}
