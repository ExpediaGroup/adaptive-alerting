package com.expedia.adaptivealerting.metrics.functions.service;

import com.expedia.adaptivealerting.anomdetect.util.HttpClientWrapper;
import com.expedia.adaptivealerting.metrics.functions.source.MetricFunctionsReader;
import com.expedia.adaptivealerting.metrics.functions.source.MetricFunctionsSpec;
import com.expedia.metrics.MetricData;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.http.entity.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.apache.http.client.fluent.Content;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MetricQueryServiceTest {

        @Mock
        private HttpClientWrapper httpClient;

        private Map<String, String> graphiteHeaders = Collections.emptyMap();
        private Map<String, String> metrictankHeaders = Collections.singletonMap("x-org-id", "1");

        private MetricFunctionsSpec metricFunctionsSpec;

        private Config metricSourceSinkConfig;

        public static Map<String, String> metricSourceSinkConfigMap;

        static {
                metricSourceSinkConfigMap = new HashMap<>();
                metricSourceSinkConfigMap.put("urlTemplate", "http://graphite/render?format=json&target=");
                metricSourceSinkConfigMap.put("is-graphite-server-metrictank", "false");
                metricSourceSinkConfigMap.put("metric-source", "graphite");
        }

        public static String readFile(String filePath) {
                String content = "";

                try {
                        content = new String(Files
                                        .readAllBytes(Paths.get(ClassLoader.getSystemResource(filePath).getPath())));
                } catch (IOException e) {
                        e.printStackTrace();
                }
                return content;
        }

        @Before
        public void setup() throws Exception {
                MockitoAnnotations.initMocks(this);
                metricSourceSinkConfig = ConfigFactory.parseMap(metricSourceSinkConfigMap);
                String functionsInputFile = "config/functions-test.txt";
                metricFunctionsSpec = MetricFunctionsReader
                                .readFromInputFile(ClassLoader.getSystemResource(functionsInputFile).getPath()).get(0);
                String validGraphiteResponse = readFile("tests/validGraphiteResponse.json");
                Content validGraphiteResponseContent = new Content(validGraphiteResponse.getBytes(),
                                ContentType.APPLICATION_JSON);
                when(httpClient.get(
                                "http://graphite/render?format=json&target=sumSeries(a.b.c)&from=1583039039&until=1583039099",
                                graphiteHeaders)).thenReturn(validGraphiteResponseContent);
                when(httpClient.get(
                                "http://graphite/render?format=json&target=sumSeries(a.b.c)&from=1583039039&until=1583039099",
                                metrictankHeaders)).thenReturn(validGraphiteResponseContent);
                when(httpClient.get(
                                "http://graphite/render?format=json&target=sumSeries(d.e.f)&from=1583039039&until=1583039099",
                                graphiteHeaders)).thenReturn(validGraphiteResponseContent);

                String validGraphiteResponseWithNull = readFile("tests/validGraphiteResponseWithNull.json");
                Content validGraphiteResponseWithNullContent = new Content(validGraphiteResponseWithNull.getBytes(),
                                ContentType.APPLICATION_JSON);
                when(httpClient.get(
                                "http://graphite/render?format=json&target=sumSeries(a.b.c)&from=1583125439&until=1583125499",
                                graphiteHeaders)).thenReturn(validGraphiteResponseWithNullContent);

                String invalidGraphiteResponse = readFile("tests/invalidGraphiteResponse.json");
                Content invalidGraphiteResponseContent = new Content(invalidGraphiteResponse.getBytes(),
                                ContentType.APPLICATION_JSON);
                when(httpClient.get(
                                "http://graphite/render?format=json&target=sumSeries(a.b.c)&from=1583211839&until=1583211899",
                                graphiteHeaders)).thenReturn(invalidGraphiteResponseContent);

                Content emptyGraphiteResponseContent = new Content("[]".getBytes(), ContentType.APPLICATION_JSON);
                when(httpClient.get(
                                "http://graphite/render?format=json&target=sumSeries(a.b.c)&from=1583298239&until=1583298299",
                                graphiteHeaders)).thenReturn(emptyGraphiteResponseContent);
        }

        @Test
        public void testValidGraphiteMetricQueryResult() throws Exception {
                Instant fixedInstant = Instant.parse("2020-03-01T05:05:39Z");
                MetricQueryService metricQueryService = new MetricQueryService(httpClient);
                MetricData metricDataResult = metricQueryService.queryMetricSource(metricSourceSinkConfig,
                                metricFunctionsSpec, fixedInstant);
                assertEquals(12.0, metricDataResult.getValue(), 0.1);
                assertEquals(1583039100, metricDataResult.getTimestamp());
                Map<String, String> tags = metricDataResult.getMetricDefinition().getTags().getKv();
                assertEquals(6, tags.size());
                assertEquals("sample_app1", tags.get("app_name"));
                assertEquals("test", tags.get("env"));
                assertEquals("custom_tag_value", tags.get("custom_tag"));
                assertEquals("sum", tags.get("aggregatedBy"));
                assertEquals("sumSeries(a.b.c)", tags.get("name"));
                assertEquals("added_tag_value", tags.get("added_tag"));
        }

        @Test
        public void testMetricQueryService() throws Exception {
                MetricQueryService metricQueryService = new MetricQueryService();
                assertTrue(metricQueryService != null);
        }

        @Test
        public void testValidGraphiteMetricQueryResultMergeTagsFalse() throws Exception {
                String functionsInputFile = "config/functions-mergeTags-false-test.txt";
                MetricFunctionsSpec metricFunctionsSpec = MetricFunctionsReader
                                .readFromInputFile(ClassLoader.getSystemResource(functionsInputFile).getPath()).get(0);

                Instant fixedInstant = Instant.parse("2020-03-01T05:05:39Z");
                MetricQueryService metricQueryService = new MetricQueryService(httpClient);
                MetricData metricDataResult = metricQueryService.queryMetricSource(metricSourceSinkConfig,
                                metricFunctionsSpec, fixedInstant);
                assertEquals(12.0, metricDataResult.getValue(), 0.1);
                assertEquals(1583039100, metricDataResult.getTimestamp());
                Map<String, String> tags = metricDataResult.getMetricDefinition().getTags().getKv();
                assertEquals(2, tags.size());
                assertEquals("sample_app2", tags.get("app_name"));
                assertEquals("test", tags.get("env"));
        }

        @Test
        public void testValidMetrictankMetricQueryResult() throws Exception {
                Map<String, String> metrictankMetricSourceSinkConfigMap = new HashMap<>();
                metrictankMetricSourceSinkConfigMap.put("urlTemplate", "http://graphite/render?format=json&target=");
                metrictankMetricSourceSinkConfigMap.put("is-graphite-server-metrictank", "metrictank");
                metrictankMetricSourceSinkConfigMap.put("metric-source", "graphite");
                Config metrictankMetricSourceSinkConfig = ConfigFactory.parseMap(metrictankMetricSourceSinkConfigMap);
                Instant fixedInstant = Instant.parse("2020-03-01T05:05:39Z");
                MetricQueryService metricQueryService = new MetricQueryService(httpClient);
                MetricData metricDataResult = metricQueryService.queryMetricSource(metrictankMetricSourceSinkConfig,
                                metricFunctionsSpec, fixedInstant);
                assertEquals(12.0, metricDataResult.getValue(), 0.1);
                assertEquals(1583039100, metricDataResult.getTimestamp());
                Map<String, String> tags = metricDataResult.getMetricDefinition().getTags().getKv();
                assertEquals(6, tags.size());
                assertEquals("sample_app1", tags.get("app_name"));
                assertEquals("test", tags.get("env"));
                assertEquals("custom_tag_value", tags.get("custom_tag"));
                assertEquals("sum", tags.get("aggregatedBy"));
                assertEquals("sumSeries(a.b.c)", tags.get("name"));
                assertEquals("added_tag_value", tags.get("added_tag"));
        }

        @Test
        public void testValidGraphiteMetricQueryResultWithNull() throws Exception {
                Instant fixedInstant = Instant.parse("2020-03-02T05:05:39Z");
                MetricQueryService metricQueryService = new MetricQueryService(httpClient);
                MetricData metricDataResult = metricQueryService.queryMetricSource(metricSourceSinkConfig,
                                metricFunctionsSpec, fixedInstant);
                assertEquals(155.0, metricDataResult.getValue(), 0.1);
                assertEquals(1583125500, metricDataResult.getTimestamp());
        }

        @Test
        public void testInvalidGraphiteMetricQueryResult() {
                try {
                        Instant fixedInstant = Instant.parse("2020-03-03T05:05:39Z");
                        MetricQueryService metricQueryService = new MetricQueryService(httpClient);
                        metricQueryService.queryMetricSource(metricSourceSinkConfig, metricFunctionsSpec, fixedInstant);
                } catch (MetricQueryServiceException e) {
                        assertTrue(true);
                }
        }

        @Test
        public void testInvalidMetricQuerySource() {
                try {
                        Map<String, String> invalidMetricSourceSinkConfigMap = new HashMap<>();
                        invalidMetricSourceSinkConfigMap.put("urlTemplate",
                                        "http://graphite/render?format=json&target=");
                        invalidMetricSourceSinkConfigMap.put("metric-source", "bad-metric-source");
                        Config invalidMetricSourceSinkConfig = ConfigFactory.parseMap(invalidMetricSourceSinkConfigMap);
                        Instant fixedInstant = Instant.parse("2020-03-03T05:05:39Z");
                        MetricQueryService metricQueryService = new MetricQueryService(httpClient);
                        metricQueryService.queryMetricSource(invalidMetricSourceSinkConfig, metricFunctionsSpec, fixedInstant);
                } catch (IllegalStateException e) {
                                        
                        assertTrue(true);
                }
        }

        @Test
        public void testEmptyGraphiteMetricQueryResult() {
                try {
                        Instant fixedInstant = Instant.parse("2020-03-04T05:05:00Z");
                        MetricQueryService metricQueryService = new MetricQueryService(httpClient);
                        metricQueryService.queryMetricSource(metricSourceSinkConfig, metricFunctionsSpec, fixedInstant);
                } catch (MetricQueryServiceException e) {
                        assertTrue(true);
                }
        }

}
