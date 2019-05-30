/*
 * Copyright 2018-2019 Expedia Group, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expedia.adaptivealerting.modelservice.util;

import com.expedia.adaptivealerting.modelservice.elasticsearch.ElasticSearchClient;
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
