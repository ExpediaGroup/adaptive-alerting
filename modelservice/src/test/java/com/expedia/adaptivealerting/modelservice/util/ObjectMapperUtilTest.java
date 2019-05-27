package com.expedia.adaptivealerting.modelservice.util;

import com.expedia.adaptivealerting.modelservice.test.ObjectMother;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

    @Test
    public void testConvertToObject() {
        Object actualObject = objectMapperUtil.convertToObject("", new TypeReference<Map>() {
        });
        assertEquals(expectedObject, actualObject);
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
