package com.expedia.adaptivealerting.anomdetect.source.data.graphite;

import com.expedia.adaptivealerting.anomdetect.util.HttpClientWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.fluent.Content;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static com.expedia.adaptivealerting.anomdetect.source.data.graphite.GraphiteClient.FETCH_METRICS_PATH;
import static org.mockito.Mockito.when;

@Slf4j
public class GraphiteClientTest {
    private static final String BASE_URI = "http://graphite";
    private static final String METRIC_URI = uri(FETCH_METRICS_PATH, "1d", 288, "metricName");
    private static final String METRIC_URI_CANT_GET = uri(FETCH_METRICS_PATH, "1d", 288, "metricNameCantGet");
    private static final String METRIC_URI_CANT_READ = uri(FETCH_METRICS_PATH, "1d", 288, "metricNameCantRead");

    private GraphiteClient clientUnderTest;

    @Mock
    private HttpClientWrapper httpClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Content docsContent;

    @Mock
    private Content docContent_cantRead;

    private byte[] docsBytes = "docsBytes".getBytes();
    private byte[] docBytes_cantRead = "docBytes_cantRead".getBytes();
    private GraphiteResult[] docs = {};
    private String from = "1d";
    private Integer maxDataPoints = 288;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        initMetricData();
        this.clientUnderTest = new GraphiteClient(BASE_URI, httpClient, objectMapper);
    }

    @Test
    public void testGetMetricData() {
        clientUnderTest.getData(from, maxDataPoints, "metricName");
    }

    @Test(expected = RuntimeException.class)
    public void testGetMetricData_cant_get() {
        clientUnderTest.getData(from, maxDataPoints, "metricNameCantGet");
    }

    @Test(expected = RuntimeException.class)
    public void testGetMetricData_cant_read() {
        clientUnderTest.getData(from, maxDataPoints, "metricNameCantRead");
    }

    private void initMetricData() throws IOException {
        when(httpClient.get(METRIC_URI)).thenReturn(docsContent);
        when(docsContent.asBytes()).thenReturn(docsBytes);
        when(objectMapper.readValue(docsBytes, GraphiteResult[].class)).thenReturn(docs);

        when(httpClient.get(METRIC_URI_CANT_GET)).thenThrow(new IOException());

        when(httpClient.get(METRIC_URI_CANT_READ)).thenReturn(docContent_cantRead);
        when(docContent_cantRead.asBytes()).thenReturn(docBytes_cantRead);
        when(objectMapper.readValue(docBytes_cantRead, GraphiteResult[].class)).thenThrow(new IOException());
    }

    private static String uri(String path, Object param, Object param1, Object param2) {
        return String.format(BASE_URI + path, param, param1, param2);
    }

}
