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
package com.expedia.adaptivealerting.modelservice.service;

import com.expedia.adaptivealerting.modelservice.entity.ElasticsearchDetector;
import com.expedia.adaptivealerting.modelservice.elasticsearch.ElasticSearchClient;
import com.expedia.adaptivealerting.modelservice.util.DateUtil;
import com.expedia.adaptivealerting.modelservice.util.ElasticsearchUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service to fetch and modify detectors stored in elastic search
 */
@Slf4j
@Service
public class DetectorServiceImpl implements DetectorService {

    private static final String DETECTOR_INDEX = "detectors";
    private static final String DETECTOR_DOC_TYPE = "detector";

    @Autowired
    private ElasticSearchClient elasticSearchClient;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<ElasticsearchDetector> findByUuid(String uuid) {
        val queryBuilder = QueryBuilders.matchQuery("uuid", uuid);
        val searchSourceBuilder = ElasticsearchUtil.getSourceBuilder(queryBuilder);
        val searchRequest = ElasticsearchUtil.getSearchRequest(searchSourceBuilder, DETECTOR_INDEX, DETECTOR_DOC_TYPE);
        return getDetectorsFromElasticSearch(searchRequest);
    }

    @Override
    public List<ElasticsearchDetector> findByCreatedBy(String user) {
        val queryBuilder = QueryBuilders.matchQuery("createdBy", user);
        val searchSourceBuilder = ElasticsearchUtil.getSourceBuilder(queryBuilder);
        val searchRequest = ElasticsearchUtil.getSearchRequest(searchSourceBuilder, DETECTOR_INDEX, DETECTOR_DOC_TYPE);
        return getDetectorsFromElasticSearch(searchRequest);
    }

    @Override
    public void toggleDetector(String uuid, Boolean enabled) {
        val elasticsearchDetector = findByUuid(uuid).get(0);
        val updateRequest = new UpdateRequest(DETECTOR_INDEX, DETECTOR_DOC_TYPE, elasticsearchDetector.getUuid());
        updateRequest.doc("enabled", enabled);
        try {
            elasticSearchClient.update(updateRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(String.format("Updating elastic search failed", e));
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ElasticsearchDetector> getLastUpdatedDetectors(int interval) {
        val now = DateUtil.now().toInstant();
        val fromDate = DateUtil.toUtcDateString((now.minus(interval, ChronoUnit.MINUTES)));
        val toDate = DateUtil.toUtcDateString(now);

        val queryBuilder = QueryBuilders.rangeQuery("lastUpdateTimestamp").from(fromDate).to(toDate);
        val searchSourceBuilder = ElasticsearchUtil.getSourceBuilder(queryBuilder);
        val searchRequest = ElasticsearchUtil.getSearchRequest(searchSourceBuilder, DETECTOR_INDEX, DETECTOR_DOC_TYPE);
        return getDetectorsFromElasticSearch(searchRequest);
    }

    private List<ElasticsearchDetector> getDetectorsFromElasticSearch(SearchRequest searchRequest) {
        SearchResponse response;
        try {
            response = elasticSearchClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("Error ES lookup", e);
            throw new RuntimeException(e);
        }

        SearchHit[] hits = response.getHits().getHits();
        List<ElasticsearchDetector> elasticsearchDetectors = new ArrayList<>();
        for (val hit : hits) {
            ElasticsearchDetector elasticsearchDetector = getElasticSearchDetector(hit.getSourceAsString(), Optional.of(hit.getFields()));
            elasticsearchDetectors.add(elasticsearchDetector);
        }
        return elasticsearchDetectors;
    }

    private ElasticsearchDetector getElasticSearchDetector(String json, Optional<Map<String, DocumentField>> documentFieldMap) {
        ElasticsearchDetector elasticsearchDetector;
        try {
            elasticsearchDetector = objectMapper.readValue(json, ElasticsearchDetector.class);
        } catch (IOException e) {
            log.error(String.format("Deserialization error", json, e));
            throw new RuntimeException(e);
        }

        val newElasticSearchDetector = new ElasticsearchDetector();
        newElasticSearchDetector.setUuid(elasticsearchDetector.getUuid());
        newElasticSearchDetector.setCreatedBy(elasticsearchDetector.getCreatedBy());
        newElasticSearchDetector.setDetectorConfig(elasticsearchDetector.getDetectorConfig());
        newElasticSearchDetector.setEnabled(elasticsearchDetector.getEnabled());
        newElasticSearchDetector.setLastUpdateTimestamp(elasticsearchDetector.getLastUpdateTimestamp());
        return newElasticSearchDetector;
    }
}