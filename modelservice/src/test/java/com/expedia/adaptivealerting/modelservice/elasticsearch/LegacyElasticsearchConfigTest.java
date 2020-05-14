package com.expedia.adaptivealerting.modelservice.elasticsearch;

import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

public class LegacyElasticsearchConfigTest {

    @InjectMocks
    private LegacyElasticsearchConfig elasticSearchConfig;

    @Mock
    private ElasticSearchProperties elasticSearchProperties;

    private ElasticSearchProperties.Config config;

    @Before
    public void setUp() {
        this.elasticSearchConfig = new LegacyElasticsearchConfig();
        MockitoAnnotations.initMocks(this);
        initTestObjects();
    }

    @Test
    public void testBuildRestClient() {
        when(elasticSearchProperties.getConfig()).thenReturn(config);
        when(elasticSearchProperties.getUrls()).thenReturn("localhost:8000");
        val elasticsearchRestClient = elasticSearchConfig.buildRestClient();
        assertNotNull(elasticsearchRestClient);
    }

    private void initTestObjects() {
        this.config = new ElasticSearchProperties.Config();
        config.setConnectionTimeout(100);
        config.setConnectionRetryTimeout(100);
        config.setConnectionRetryTimeout(100);
        config.setMaxTotalConnections(50);
    }
}
