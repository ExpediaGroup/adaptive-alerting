package com.expedia.adaptivealerting.modelservice.util;

import lombok.val;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;



public class ElasticsearchUtil {

    public static SearchSourceBuilder getSourceBuilder(QueryBuilder queryBuilder) {
        val searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        return searchSourceBuilder;
    }

    public static SearchRequest getSearchRequest(SearchSourceBuilder searchSourceBuilder, String indexName, String docType) {
        val searchRequest = new SearchRequest();
        return searchRequest.source(searchSourceBuilder).indices(indexName).types(docType);
    }
}
