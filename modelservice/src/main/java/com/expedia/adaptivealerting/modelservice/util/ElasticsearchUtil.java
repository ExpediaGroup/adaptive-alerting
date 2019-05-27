package com.expedia.adaptivealerting.modelservice.util;

import com.expedia.adaptivealerting.modelservice.elasticsearch.ElasticSearchClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class ElasticsearchUtil {

    @Autowired
    private ElasticSearchClient elasticSearchClient;

    public IndexResponse getIndexResponse(IndexRequest indexRequest, String json) {
        try {
            indexRequest.source(json, XContentType.JSON);
            return elasticSearchClient.index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(String.format("Indexing mapping %s failed", json, e));
            throw new RuntimeException(e);
        }
    }

    public SearchSourceBuilder getSourceBuilder(QueryBuilder queryBuilder) {
        val searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        return searchSourceBuilder;
    }

    public SearchRequest getSearchRequest(SearchSourceBuilder searchSourceBuilder, String indexName, String docType) {
        val searchRequest = new SearchRequest();
        return searchRequest.source(searchSourceBuilder).indices(indexName).types(docType);
    }
}
