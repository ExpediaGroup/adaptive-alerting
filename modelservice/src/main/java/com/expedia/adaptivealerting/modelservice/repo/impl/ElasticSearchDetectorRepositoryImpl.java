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

import com.expedia.adaptivealerting.modelservice.entity.ElasticSearchDetector;
import com.expedia.adaptivealerting.modelservice.repo.ElasticSearchDetectorRepoCustom;
import com.expedia.adaptivealerting.modelservice.util.DateUtil;
import lombok.val;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.UpdateQueryBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ElasticSearchDetectorRepositoryImpl implements ElasticSearchDetectorRepoCustom {

    private static final int RESULTS_SIZE = 500;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private Client elasticSearchClient;

    @Override
    public void toggleDetector(ElasticSearchDetector detector, Boolean enabled) {
        val indexRequest = new IndexRequest();
        indexRequest.source("enabled", enabled);
        val updateQuery = new UpdateQueryBuilder().withId(detector.getId()).withClass(ElasticSearchDetector.class).withIndexRequest(indexRequest).build();
        elasticsearchTemplate.update(updateQuery);
    }

    @Override
    public List<ElasticSearchDetector> getLastUpdatedDetectors(String fromDate, String toDate) {

        val matchDocumentsWithinRange = buildQueryBuilder(fromDate, toDate);
        val hits = getElasticSearchResponse(matchDocumentsWithinRange).getHits();
        List<ElasticSearchDetector> elasticSearchDetectors = new ArrayList<>();

        for (val hit : hits) {
            val source = hit.getSource();
            elasticSearchDetectors.add(getElasticSearchDetector(source));
        }
        return elasticSearchDetectors;
    }

    private RangeQueryBuilder buildQueryBuilder(String fromDate, String toDate) {
        return QueryBuilders.rangeQuery("lastUpdateTimestamp")
                .lte(toDate)
                .gte(fromDate);
    }

    private SearchResponse getElasticSearchResponse(RangeQueryBuilder queryBuilder) {
        return elasticSearchClient.prepareSearch()
                .setSize(RESULTS_SIZE)
                .setQuery(queryBuilder)
                .execute().actionGet();
    }

    private ElasticSearchDetector getElasticSearchDetector(Map<String, Object> source) {

        val elasticSearchDetector = new ElasticSearchDetector();
        elasticSearchDetector.setId((String) source.get("id"));
        elasticSearchDetector.setCreatedBy((String) source.get("createdBy"));
        elasticSearchDetector.setUuid((String) source.get("uuid"));
        elasticSearchDetector.setDetectorConfig((Map<String, Object>) source.get("detectorConfig"));
        elasticSearchDetector.setEnabled((Boolean) source.get("enabled"));
        elasticSearchDetector.setLastUpdateTimestamp(DateUtil.toUTCDate((String) source.get("lastUpdateTimestamp")));
        return elasticSearchDetector;
    }
}