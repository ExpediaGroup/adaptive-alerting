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
package com.expedia.adaptivealerting.modelservice.dao.es;

import lombok.Generated;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * This is a wrapper class around {@code RestHighLevelClient}.
 * we can't easily mock methods of class {@code RestHighLevelClient}, which are final.
 * So this wrapper class will help to address the issue.
 */
@Component
@Generated // (excluding from code coverage)
public class ElasticSearchClient {

    @Autowired
    private RestHighLevelClient client;

    public IndexResponse index(IndexRequest indexRequest, RequestOptions options) throws IOException {
        return client.index(indexRequest, options);
    }

    public IndicesClient indices() {
        return client.indices();
    }

    public DeleteResponse delete(DeleteRequest deleteRequest, RequestOptions options) throws IOException {
        return client.delete(deleteRequest, options);
    }

    public GetResponse get(GetRequest getRequest, RequestOptions options) throws IOException {
        return client.get(getRequest, options);
    }

    public SearchResponse search(SearchRequest searchRequest, RequestOptions options) throws IOException {
        return client.search(searchRequest, options);
    }
}
