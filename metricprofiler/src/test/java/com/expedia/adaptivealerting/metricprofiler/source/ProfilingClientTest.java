package com.expedia.adaptivealerting.metricprofiler.source;

import com.expedia.adaptivealerting.anomdetect.util.HttpClientWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.http.client.fluent.Content;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class ProfilingClientTest {
    private static final String BASE_URI = "http://example.com";

    private ProfilingClient clientUnderTest;

    @Mock
    private HttpClientWrapper httpClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ObjectMapper objectMapper_exception;

    @Mock
    private HttpClientWrapper httpClient_exception;

    private Map<String, String> metricTags;

    @Mock
    private Content docContent;

    private byte[] docBytes = "docBytes".getBytes();
    private String tagsBody = "tagsBody";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.clientUnderTest = new ProfilingClient(httpClient, objectMapper, BASE_URI);
        initTestObjects();
        initDependencies();
    }

    @Test
    public void testFindProfilingDocument() {
        metricTags.put("app", "test-app");
        val result = clientUnderTest.findProfilingDocument(metricTags);
        assertEquals(true, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindProfilingDocument_illegal_arguments() {
        val result = clientUnderTest.findProfilingDocument(metricTags);
        assertEquals(true, result);
    }

    @Test(expected = RuntimeException.class)
    public void testFindProfilingDocument_mapper_io_exception() throws IOException {
        clientUnderTest.findProfilingDocument(metricTags);
    }

    @Test(expected = RuntimeException.class)
    @SneakyThrows
    public void testFindProfilingDocument_http_io_exception() {
        clientUnderTest.findProfilingDocument(metricTags);
    }

    private void initTestObjects() {
        this.metricTags = new HashMap<>();
    }

    @SneakyThrows
    private void initDependencies() {
        when(objectMapper.writeValueAsString(metricTags)).thenReturn(tagsBody);
        when(objectMapper_exception.writeValueAsString(metricTags)).thenThrow(new IOException());
        when(httpClient.post(anyString(), anyString())).thenReturn(docContent);
        when(httpClient_exception.post(anyString(), anyString())).thenThrow(new IOException());
        when(docContent.asBytes()).thenReturn(docBytes);
        when(objectMapper.readValue(docBytes, Boolean.class)).thenReturn(true);
    }

}
