package com.expedia.adaptivealerting.modelservice.elasticsearch;

import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

public class ElasticsearchConfigTest {

    @InjectMocks
    private ElasticsearchConfig elasticSearchConfig;

    @Mock
    private ElasticSearchProperties elasticSearchProperties;

    private ElasticSearchProperties.Config config;

    @Before
    public void setUp() {
        this.elasticSearchConfig = new ElasticsearchConfig();
        MockitoAnnotations.initMocks(this);
        initTestObjects();
    }

    @Test
    public void testRestTemplate() {
        when(elasticSearchProperties.getUrls()).thenReturn("localhost:8000");
        when(elasticSearchProperties.getConfig()).thenReturn(config);
        val elasticsearchRestTemplate = elasticSearchConfig.elasticsearchTemplate();
        assertNotNull(elasticsearchRestTemplate);
    }

    private void initTestObjects() {
        this.config = new ElasticSearchProperties.Config();
        config.setConnectionTimeout(100);
        config.setConnectionRetryTimeout(100);
        config.setConnectionRetryTimeout(100);
        config.setMaxTotalConnections(50);
    }
}
