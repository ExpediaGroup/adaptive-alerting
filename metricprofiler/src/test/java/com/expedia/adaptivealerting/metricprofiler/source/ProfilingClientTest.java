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

import static com.expedia.adaptivealerting.metricprofiler.source.ProfilingClient.FIND_DOCUMENT_PATH;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class ProfilingClientTest {
    private static final String BASE_URI = "http://example.com";
    private static final String FIND_DOC_URI = uri(FIND_DOCUMENT_PATH);

    private ProfilingClient clientUnderTest;

    @Mock
    private HttpClientWrapper httpClient;

    @Mock
    private ObjectMapper objectMapper;

    private Map<String, String> metricTags;
    private Map<String, String> metricTags_cantRead;
    private Map<String, String> metricTags_invalidContent;

    @Mock
    private Content docContent;

    @Mock
    private Content invalidDocContent;

    private byte[] docBytes = "docBytes".getBytes();
    private byte[] invalidDocBytes = "invalidDocBytes".getBytes();
    private String tagsBody = "tagsBody";
    private String tagsBody_cantRead = "illegalTagsBody";
    private String tagsBody_invalidContent = "invalidContent";

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
    public void testFindProfilingDocument_http_io_exception() {
        metricTags_cantRead.put("app", "test-app");
        clientUnderTest.findProfilingDocument(metricTags_cantRead);
    }

    @Test(expected = RuntimeException.class)
    public void testFindProfilingDocument_http_io_exception_1() {
        metricTags_invalidContent.put("app", "test-app");
        clientUnderTest.findProfilingDocument(metricTags_invalidContent);
    }

    private void initTestObjects() {
        this.metricTags = new HashMap<>();
        this.metricTags_cantRead = new HashMap<>();
        this.metricTags_invalidContent = new HashMap<>();
    }

    @SneakyThrows
    private void initDependencies() {
        when(objectMapper.writeValueAsString(metricTags)).thenReturn(tagsBody);
        when(httpClient.post(FIND_DOC_URI, tagsBody)).thenReturn(docContent);
        when(docContent.asBytes()).thenReturn(docBytes);
        when(objectMapper.readValue(docBytes, Boolean.class)).thenReturn(true);

        when(objectMapper.writeValueAsString(metricTags_cantRead)).thenReturn(tagsBody_cantRead);
        when(httpClient.post(FIND_DOC_URI, tagsBody_cantRead)).thenThrow(new IOException());

        when(objectMapper.writeValueAsString(metricTags_invalidContent)).thenReturn(tagsBody_invalidContent);
        when(httpClient.post(FIND_DOC_URI, tagsBody_invalidContent)).thenReturn(invalidDocContent);
        when(invalidDocContent.asBytes()).thenReturn(invalidDocBytes);
        when(objectMapper.readValue(invalidDocBytes, Boolean.class)).thenThrow(new IOException());
    }

    private static String uri(String path) {
        return String.format(BASE_URI + path);
    }

}