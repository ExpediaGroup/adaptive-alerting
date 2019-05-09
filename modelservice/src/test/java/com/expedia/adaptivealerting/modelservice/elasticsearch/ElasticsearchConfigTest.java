package com.expedia.adaptivealerting.modelservice.elasticsearch;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.Client;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class ElasticsearchConfigTest {

    @InjectMocks
    private ElasticsearchConfig elasticsearchConfig;

    @Mock
    private ElasticsearchSettings elasticsearchSettings;

    @Test
    public void testGetClient() {
        Client elasticsearchClient = elasticsearchConfig.client();
        assertEquals(PreBuiltTransportClient.class, elasticsearchClient.getClass());
        assertNotNull(elasticsearchClient);
    }

    @Test
    public void testGetElasticsearchTemplate() {
        ElasticsearchOperations elasticsearchTemplate = elasticsearchConfig.elasticsearchTemplate();
        assertNotNull(elasticsearchTemplate);
    }
}
