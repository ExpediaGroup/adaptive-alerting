package com.expedia.adaptivealerting.kafka.visualizer;

import com.typesafe.config.Config;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;

public class ElasticSearchClientTest {

    private ElasticSearchClient elasticSearchClient;

    @Before
    public void setUp() {
        elasticSearchClient = new ElasticSearchClient();
    }

    @Test
    public void testRestClientBuilder() {
        Config esConfig = VisualizerUtility.getConfig("elastic-search");
        RestHighLevelClient restHighLevelClient = elasticSearchClient.restClientBuilder(esConfig);
        assertNotNull(restHighLevelClient);
    }

    @Test
    public void testRestClientBuilderWrongConfig() {
        Config esConfig = VisualizerUtility.getConfig("metric-consumer");
        RestHighLevelClient restHighLevelClient = elasticSearchClient.restClientBuilder(esConfig);
        assertNull(restHighLevelClient);
    }

}
