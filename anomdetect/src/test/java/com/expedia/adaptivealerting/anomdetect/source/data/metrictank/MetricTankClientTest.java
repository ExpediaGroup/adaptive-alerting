package com.expedia.adaptivealerting.anomdetect.source.data.metrictank;

import com.expedia.adaptivealerting.anomdetect.util.HttpClientWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.fluent.Content;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.expedia.adaptivealerting.anomdetect.source.data.metrictank.MetricTankClient.FETCH_METRICS_PATH;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@Slf4j
public class MetricTankClientTest {
    private static final String BASE_URI = "http://graphite";
    private static final String METRIC_URI = fetchMetricsUri("metricName");
    private static final String METRIC_URI_CANT_GET = fetchMetricsUri("metricNameCantGet");
    private static final String METRIC_URI_CANT_READ = fetchMetricsUri("metricNameCantRead");
    private static final int ONE_DAY_IN_SECONDS = 60 * 60 * 24;
    private static final int FROM_TIME_IN_SECONDS = 1580815495;
    private static final int UNTIL_TIME_IN_SECONDS = FROM_TIME_IN_SECONDS + ONE_DAY_IN_SECONDS;

    private MetricTankClient clientUnderTest;

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
    private MetricTankResult[] docs = {};
    private Map<String, String> headers = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        initMetricData();
        this.clientUnderTest = new MetricTankClient(BASE_URI, httpClient, objectMapper);
    }

    @Test
    public void testGetMetricData() {
        assertEquals(1580901895, UNTIL_TIME_IN_SECONDS);
        clientUnderTest.getData(FROM_TIME_IN_SECONDS, UNTIL_TIME_IN_SECONDS, "metricName");
    }

    @Test(expected = MetricTankClientException.class)
    public void testGetMetricData_cant_get() {
        clientUnderTest.getData(FROM_TIME_IN_SECONDS, UNTIL_TIME_IN_SECONDS, "metricNameCantGet");
    }

    @Test(expected = MetricTankClientException.class)
    public void testGetMetricData_cant_read() {
        clientUnderTest.getData(FROM_TIME_IN_SECONDS, UNTIL_TIME_IN_SECONDS, "metricNameCantRead");
    }

    private void initMetricData() throws IOException {
        headers.put("x-org-id", "1");
        when(httpClient.get(METRIC_URI, headers)).thenReturn(docsContent);
        when(docsContent.asBytes()).thenReturn(docsBytes);
        when(objectMapper.readValue(docsBytes, MetricTankResult[].class)).thenReturn(docs);

        when(httpClient.get(METRIC_URI_CANT_GET, headers)).thenThrow(new IOException());

        when(httpClient.get(METRIC_URI_CANT_READ, headers)).thenReturn(docContent_cantRead);
        when(docContent_cantRead.asBytes()).thenReturn(docBytes_cantRead);
        when(objectMapper.readValue(docBytes_cantRead, MetricTankResult[].class)).thenThrow(new IOException());
    }


    private static String fetchMetricsUri(String metricName) {
        return String.format(BASE_URI + FETCH_METRICS_PATH, FROM_TIME_IN_SECONDS, UNTIL_TIME_IN_SECONDS, metricName);
    }
}
