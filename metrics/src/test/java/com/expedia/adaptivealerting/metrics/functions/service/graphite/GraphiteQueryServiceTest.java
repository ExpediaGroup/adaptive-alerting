package com.expedia.adaptivealerting.metrics.functions.service.graphite;

import com.expedia.adaptivealerting.anomdetect.util.HttpClientWrapper;
import com.expedia.adaptivealerting.metrics.functions.TypesafeConfigLoader;
import com.expedia.adaptivealerting.metrics.functions.source.MetricFunctionsReader;
import com.expedia.adaptivealerting.metrics.functions.source.MetricFunctionsSpec;
import com.expedia.adaptivealerting.metrics.functions.source.graphite.GraphiteQueryResult;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.TagCollection;
import com.typesafe.config.Config;
import lombok.val;
import org.apache.http.entity.ContentType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.apache.http.client.fluent.Content;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GraphiteQueryServiceTest {

    @Mock
    private HttpClientWrapper httpClientWrapper;

    private MetricData validMetricData;

    private MetricData defaultMetricData;

    private MetricFunctionsSpec metricFunctionsSpec;

    private Config metricSourceSinkConfig;

    private GraphiteQueryService graphiteQueryService;

    private double METRIC_DEFAULT_VALUE = 0.0;
    private static final String METRIC_SOURCE_SINK = "metric-source-sink";
    private static final String APP_ID = "aa-metric-functions-test";
    private Map<String, String> metricTankOrgIdHeader = Collections.singletonMap("x-org-id", "1");





    @Before
    public void initMetricSourceSinkConfigSetup() throws Exception {
        MockitoAnnotations.initMocks(this);
        val config = new TypesafeConfigLoader(APP_ID).loadMergedConfig();
        metricSourceSinkConfig = config.getConfig(METRIC_SOURCE_SINK);
        val functionsInputFile = "config/functions-test.txt";
        metricFunctionsSpec = MetricFunctionsReader.readFromInputFile(
                ClassLoader.getSystemResource(functionsInputFile).getPath()).get(0);
        val testDatapoint = "[12.0,1568255056]";
        JSONArray sampleJsonGraphite = new JSONArray();
        JSONObject sampleJsonGraphiteResult = new JSONObject();
        JSONArray testDatapoints = new JSONArray();
        JSONObject testTags = new JSONObject();
        testTags.put("aggregatedBy", "sum");
        testTags.put("name", "sumSeries(a.b.c)");
        testDatapoints.put(0, testDatapoint);
        sampleJsonGraphiteResult.put("datapoints", testDatapoints);
        sampleJsonGraphiteResult.put("target", "sumSeries(a.b.c)");
        sampleJsonGraphiteResult.put("tags", testTags);
        sampleJsonGraphite.put(0, sampleJsonGraphiteResult);
        val uri = "samplegraphitehosturi/render?until=now&format=json&target=sumSeries(a.b.c)&from=-30s";
        Content mockGraphiteResult = new Content(sampleJsonGraphite.toString().getBytes(), ContentType.APPLICATION_JSON);
        when(httpClientWrapper.get(uri, metricTankOrgIdHeader)).thenReturn(mockGraphiteResult);
        graphiteQueryService = new GraphiteQueryService(httpClientWrapper);
        initValidMetricDataSetup(sampleJsonGraphite.toString());
        initDefaultMetricDataSetup();
    }

    public void initValidMetricDataSetup(String sampleJsonGraphite) {
        GraphiteQueryResult graphiteQueryResult = new GraphiteQueryResult();
        graphiteQueryResult.getGraphiteQueryResultFromJson(sampleJsonGraphite);
        String graphiteKey = graphiteQueryResult.getTags().get("name");
        HashMap<String, String> tagsBuilder = new HashMap<>();
        tagsBuilder.putAll(metricFunctionsSpec.getTags());
        tagsBuilder.putAll(graphiteQueryResult.getTags());
        TagCollection tags = new TagCollection(tagsBuilder);
        TagCollection meta = TagCollection.EMPTY;
        MetricDefinition metricDefinition = new MetricDefinition(graphiteKey, tags, meta);
        validMetricData = new MetricData(metricDefinition, graphiteQueryResult.getDatapoint().getValue(),
                graphiteQueryResult.getDatapoint().getTimestamp());
    }

    public void initDefaultMetricDataSetup() {
        TagCollection tags = TagCollection.EMPTY;
        TagCollection meta = TagCollection.EMPTY;
        MetricDefinition metricDefinition = new MetricDefinition(("aggregator.producer." +
                metricFunctionsSpec.getFunction()),
                tags, meta);
        defaultMetricData = new MetricData(metricDefinition, METRIC_DEFAULT_VALUE,
                System.currentTimeMillis() / 1000);
    }

    public void verifyTags(Map<String, String> tagsExpected,
                           Map<String, String> tagsActual) {
        Iterator itExpected = tagsExpected.entrySet().iterator();
        while (itExpected.hasNext()) {
            Map.Entry tagExpected = (Map.Entry) itExpected.next();
            assertTrue(tagsActual.containsKey(tagExpected.getKey()));
            assertTrue(tagsActual.containsValue(tagExpected.getValue()));
        }
    }

    @Test
    public void testGetMetricQueryResultTrue() {
        MetricData metricDataResult = graphiteQueryService.queryMetricSource(metricSourceSinkConfig,
                metricFunctionsSpec);
        assertEquals(validMetricData.getValue(), metricDataResult.getValue(), 0.1);
        assertEquals(validMetricData.getTimestamp(), metricDataResult.getTimestamp());
        assertEquals(validMetricData.getMetricDefinition().getKey(),
                metricDataResult.getMetricDefinition().getKey());
        assertEquals(validMetricData.getMetricDefinition().getTags().getKv().size(),
                metricDataResult.getMetricDefinition().getTags().getKv().size());
        assertEquals(validMetricData.getMetricDefinition().getMeta().getKv().size(),
                metricDataResult.getMetricDefinition().getMeta().getKv().size());
        verifyTags(validMetricData.getMetricDefinition().getTags().getKv(),
                metricDataResult.getMetricDefinition().getTags().getKv());
    }

    @Test
    public void testGetMetricQueryResultException() throws Exception{
        HttpClientWrapper httpClientWrapper = new HttpClientWrapper();
        graphiteQueryService = new GraphiteQueryService(httpClientWrapper);
        val config = new TypesafeConfigLoader(APP_ID).loadMergedConfig();
        val metricSourceSinkConfig = config.getConfig(METRIC_SOURCE_SINK);
        MetricData metricDataResult = graphiteQueryService.queryMetricSource(metricSourceSinkConfig,
                metricFunctionsSpec);
        assertEquals(defaultMetricData.getValue(), metricDataResult.getValue(), 0.1);
        assertEquals(defaultMetricData.getMetricDefinition().getKey(),
                metricDataResult.getMetricDefinition().getKey());
        assertEquals(TagCollection.EMPTY,
                metricDataResult.getMetricDefinition().getTags());
        assertEquals(TagCollection.EMPTY,
                metricDataResult.getMetricDefinition().getMeta());
    }

     @Test
     public void testGetMetricQueryResultEmpty() throws Exception {
        val uri = "samplegraphitehosturi/render?until=now&format=json&target=sumSeries(a.b.c)&from=-30s";
        val sampleJsonGraphite = "[]";
        Content mockGraphiteResult = new Content(sampleJsonGraphite.getBytes(), ContentType.APPLICATION_JSON);
        when(httpClientWrapper.get(uri, metricTankOrgIdHeader)).thenReturn(mockGraphiteResult);
        MetricData metricDataResult = graphiteQueryService.queryMetricSource(metricSourceSinkConfig,
        metricFunctionsSpec);
        assertEquals(defaultMetricData.getValue(), metricDataResult.getValue(), 0.1);
        assertEquals(defaultMetricData.getMetricDefinition().getKey(),
                metricDataResult.getMetricDefinition().getKey());
        assertEquals(TagCollection.EMPTY,
                metricDataResult.getMetricDefinition().getTags());
        assertEquals(TagCollection.EMPTY,
                metricDataResult.getMetricDefinition().getMeta());
        }

    @Test
    public void testGetMetricQueryResultNullDatapoint() throws Exception {
        val uri = "samplegraphitehosturi/render?until=now&format=json&target=sumSeries(a.b.c)&from=-30s";
        val testDatapoint = "[null,1568255056]";
        JSONArray sampleJsonGraphite = new JSONArray();
        JSONObject sampleJsonGraphiteResult = new JSONObject();
        JSONArray testDatapoints = new JSONArray();
        JSONObject testTags = new JSONObject();
        testTags.put("aggregatedBy", "sum");
        testTags.put("name", "sumSeries(a.b.c)");
        testDatapoints.put(0, testDatapoint);
        sampleJsonGraphiteResult.put("datapoints", testDatapoints);
        sampleJsonGraphiteResult.put("target", "sumSeries(a.b.c)");
        sampleJsonGraphiteResult.put("tags", testTags);
        sampleJsonGraphite.put(0, sampleJsonGraphiteResult);
        Content mockGraphiteResult = new Content(sampleJsonGraphite.toString().getBytes(), ContentType.APPLICATION_JSON);
        when(httpClientWrapper.get(uri, metricTankOrgIdHeader)).thenReturn(mockGraphiteResult);
        MetricData metricDataResult = graphiteQueryService.queryMetricSource(metricSourceSinkConfig,
                metricFunctionsSpec);
        assertEquals(defaultMetricData.getValue(), metricDataResult.getValue(), 0.1);
        assertEquals(defaultMetricData.getMetricDefinition().getKey(),
                metricDataResult.getMetricDefinition().getKey());
        assertEquals(TagCollection.EMPTY,
                metricDataResult.getMetricDefinition().getTags());
        assertEquals(TagCollection.EMPTY,
                metricDataResult.getMetricDefinition().getMeta());
    }

}
