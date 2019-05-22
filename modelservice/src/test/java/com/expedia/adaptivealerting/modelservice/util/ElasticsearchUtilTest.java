package com.expedia.adaptivealerting.modelservice.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;

import static com.expedia.adaptivealerting.modelservice.util.ElasticsearchUtil.getSearchRequest;
import static com.expedia.adaptivealerting.modelservice.util.ElasticsearchUtil.getSourceBuilder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Slf4j
public class ElasticsearchUtilTest {

    @Test
    public void testGetSourceBuilder() {
        val queryBuilder = QueryBuilders.matchQuery("createdBy", "kashah");
        SearchSourceBuilder sourceBuilder = getSourceBuilder(queryBuilder);
        assertNotNull(sourceBuilder);
        assertEquals(sourceBuilder.getClass(), SearchSourceBuilder.class);
    }

    @Test
    public void testGetSearchRequest() {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        SearchRequest searchRequest = getSearchRequest(sourceBuilder, "index", "type");
        assertNotNull(searchRequest);
        assertEquals(searchRequest.getClass(), SearchRequest.class);
    }
}