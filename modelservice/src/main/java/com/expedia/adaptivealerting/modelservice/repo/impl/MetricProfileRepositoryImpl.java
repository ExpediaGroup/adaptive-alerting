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
package com.expedia.adaptivealerting.modelservice.repo.impl;

import com.expedia.adaptivealerting.modelservice.repo.impl.percolator.PercolatorMetricProfiling;
import com.expedia.adaptivealerting.modelservice.repo.impl.elasticsearch.ElasticSearchClient;
import com.expedia.adaptivealerting.modelservice.repo.impl.elasticsearch.ElasticSearchProperties;
import com.expedia.adaptivealerting.modelservice.repo.MetricProfileRepository;
import com.expedia.adaptivealerting.modelservice.repo.request.CreateMetricProfilingRequest;
import com.expedia.adaptivealerting.modelservice.repo.impl.elasticsearch.ElasticsearchUtil;
import com.expedia.adaptivealerting.modelservice.util.ObjectMapperUtil;
import com.expedia.adaptivealerting.modelservice.util.QueryUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.percolator.PercolateQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MetricProfileRepositoryImpl implements MetricProfileRepository {

    private static final String METRIC_PROFILING_INDEX = "profile-metrics";
    private static final String METRIC_PROFILING_DOC_TYPE = "_doc";

    @Autowired
    private ElasticSearchProperties elasticSearchProperties;

    @Autowired
    private ElasticSearchClient elasticSearchClient;

    @Autowired
    private ElasticsearchUtil elasticsearchUtil;

    @Autowired
    private ObjectMapperUtil objectMapperUtil;

    @Override
    public String createMetricProfile(CreateMetricProfilingRequest createRequest) {
        val fields = createRequest.getFields();
        val newFieldMappings = elasticsearchUtil.removeFieldsHavingExistingMapping(fields, METRIC_PROFILING_INDEX, METRIC_PROFILING_DOC_TYPE);
        elasticsearchUtil.updateIndexMappings(newFieldMappings, METRIC_PROFILING_INDEX, METRIC_PROFILING_DOC_TYPE);

        val percolatorMetricProfiling = new PercolatorMetricProfiling()
                .setProfilingTime(System.currentTimeMillis())
                .setIsStationary(createRequest.getIsStationary())
                .setQuery(QueryUtil.buildQuery(createRequest.getExpression()));

        val indexRequest = new IndexRequest(METRIC_PROFILING_INDEX, METRIC_PROFILING_DOC_TYPE);
        val json = objectMapperUtil.convertToString(percolatorMetricProfiling);
        return elasticsearchUtil.index(indexRequest, json).getId();
    }

    @Override
    public void updateMetricProfile(String id, Boolean isStationary) {
        val updateRequest = new UpdateRequest(METRIC_PROFILING_INDEX, METRIC_PROFILING_DOC_TYPE, id);
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("isStationary", isStationary);
        updateRequest.doc(jsonMap);
        try {
            elasticSearchClient.update(updateRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("Updating elastic search failed", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean profilingExists(Map<String, String> tags) {
        try {
            List<BytesReference> refList = new ArrayList<>();
            XContentBuilder xContent = XContentFactory.jsonBuilder();
            xContent.map(tags);
            refList.add(BytesReference.bytes(xContent));

            PercolateQueryBuilder percolateQuery = new PercolateQueryBuilder(PercolatorMetricProfiling.QUERY_KEYWORD,
                    refList, XContentType.JSON);

            val boolQueryBuilder = new BoolQueryBuilder();
            boolQueryBuilder.filter(percolateQuery);
            val searchSourceBuilder = elasticsearchUtil.getSourceBuilder(boolQueryBuilder);
            searchSourceBuilder.timeout(new TimeValue(elasticSearchProperties.getConfig().getConnectionTimeout()));
            searchSourceBuilder.size(500);

            val searchRequest = new SearchRequest().source(searchSourceBuilder).indices(METRIC_PROFILING_INDEX);
            val searchResponse = elasticSearchClient.search(searchRequest, RequestOptions.DEFAULT);

            return searchResponse.getHits().getHits().length > 0;
        } catch (IOException e) {
            log.error("Error ES lookup", e);
            throw new RuntimeException(e);
        }
    }
}
