package com.expedia.adaptivealerting.modelservice.repo.impl.elasticsearch;

import com.expedia.adaptivealerting.modelservice.elasticsearch.ElasticSearchProperties;
import com.expedia.adaptivealerting.modelservice.elasticsearch.ElasticsearchConfig;
import com.expedia.adaptivealerting.modelservice.exception.MissingSystemPropertyException;
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
    private ElasticSearchProperties properties;

    @Before
    public void setUp() {
        this.elasticSearchConfig = new ElasticsearchConfig();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRestTemplate() {
        when(properties.getUrls()).thenReturn("localhost:8000");
        val elasticsearchRestTemplate = elasticSearchConfig.elasticsearchTemplate();
        assertNotNull(elasticsearchRestTemplate);
    }

    @Test(expected = MissingSystemPropertyException.class)
    public void testRestTemplate_invalid_config() {
        when(properties.getUrls()).thenReturn(null);
        val restHighLevelClient = elasticSearchConfig.client();
        assertNotNull(restHighLevelClient);
    }
}
