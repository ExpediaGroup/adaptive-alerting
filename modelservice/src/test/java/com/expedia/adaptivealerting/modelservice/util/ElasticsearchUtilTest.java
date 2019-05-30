package com.expedia.adaptivealerting.modelservice.util;

import com.expedia.adaptivealerting.modelservice.elasticsearch.ElasticSearchClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
public class ElasticsearchUtilTest {

    /* Class under test */
    @InjectMocks
    private ElasticsearchUtil elasticsearchUtil;

    @Mock
    private ElasticSearchClient elasticSearchClient;

    @Before
    public void setUp() throws Exception {
        this.elasticsearchUtil = new ElasticsearchUtil();
        MockitoAnnotations.initMocks(this);
    }

    @SneakyThrows
    @Test
    public void testGetIndexResponse() {
        IndexResponse indexResponse = mock(IndexResponse.class);
        when(indexResponse.getId()).thenReturn("1");
        when(elasticSearchClient.index(any(IndexRequest.class), any(RequestOptions.class))).thenReturn(indexResponse);
        val actualIndexResponse = elasticsearchUtil.getIndexResponse(new IndexRequest(), "");
        assertNotNull(actualIndexResponse);
        assertEquals("1", actualIndexResponse.getId());
    }

    @SneakyThrows
    @Test
    public void testGetSourceBuilder() {
        val queryBuilder = QueryBuilders.matchQuery("createdBy", "kashah");
        val actualSourceBuilder = elasticsearchUtil.getSourceBuilder(queryBuilder);
        assertNotNull(actualSourceBuilder);
        assertEquals(SearchSourceBuilder.class, actualSourceBuilder.getClass());
    }

    @SneakyThrows
    @Test
    public void testGetSearchRequest() {
        val actualSearchRequest = elasticsearchUtil.getSearchRequest(new SearchSourceBuilder(), "index", "docType");
        assertNotNull(actualSearchRequest);
        assertEquals(SearchRequest.class, actualSearchRequest.getClass());
    }

}