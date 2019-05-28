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

import com.expedia.adaptivealerting.modelservice.elasticsearch.ElasticSearchClient;
import com.expedia.adaptivealerting.modelservice.entity.ElasticsearchDetector;
import com.expedia.adaptivealerting.modelservice.repo.EsDetectorRepository;
import com.expedia.adaptivealerting.modelservice.util.DateUtil;
import com.expedia.adaptivealerting.modelservice.util.ElasticsearchUtil;
import com.expedia.adaptivealerting.modelservice.util.ObjectMapperUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EsDetectorRepositoryImpl implements EsDetectorRepository {

    private static final String DETECTOR_INDEX = "detectors";
    private static final String DETECTOR_DOC_TYPE = "detector";

    @Autowired
    private ElasticSearchClient elasticSearchClient;

    @Autowired
    private ObjectMapperUtil objectMapperUtil;

    @Autowired
    private ElasticsearchUtil elasticsearchUtil;

    @Override
    public String createDetector(ElasticsearchDetector elasticsearchDetector) {
        val newElasticsearchDetector = getElasticSearchDetector(elasticsearchDetector);
        val indexRequest = new IndexRequest(DETECTOR_INDEX, DETECTOR_DOC_TYPE, elasticsearchDetector.getUuid());
        String json = objectMapperUtil.convertToString(newElasticsearchDetector);
        return elasticsearchUtil.getIndexResponse(indexRequest, json).getId();
    }

    @Override
    public void deleteDetector(String uuid) {
        val deleteRequest = new DeleteRequest(DETECTOR_INDEX,
                DETECTOR_DOC_TYPE, uuid);
        try {
            elasticSearchClient.delete(deleteRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(String.format("Deleting detector %s failed", uuid), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateDetector(String uuid, ElasticsearchDetector elasticsearchDetector) {

        val updateRequest = new UpdateRequest(DETECTOR_INDEX, DETECTOR_DOC_TYPE, uuid);
        Map<String, Object> jsonMap = new HashMap<>();

        for (Field field : elasticsearchDetector.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            String name = field.getName();
            if (!name.isEmpty()) {
                Object value;
                try {
                    value = field.get(elasticsearchDetector);
                }
                catch (IllegalAccessException e) {
                    log.error(String.format("Updating elastic search failed", e));
                    throw new RuntimeException(e);
                }
                if (name.equals("lastUpdateTimestamp")) {
                    Date d = (Date) value;
                    value = DateUtil.instantToDate(d.toInstant());
                }
                jsonMap.put(name, value);
            }
        }
        updateRequest.doc(jsonMap);
        try {
            elasticSearchClient.update(updateRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(String.format("Updating elastic search failed", e));
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ElasticsearchDetector> findByUuid(String uuid) {
        val queryBuilder = QueryBuilders.matchQuery("uuid", uuid);
        val searchSourceBuilder = elasticsearchUtil.getSourceBuilder(queryBuilder);
        val searchRequest = elasticsearchUtil.getSearchRequest(searchSourceBuilder, DETECTOR_INDEX, DETECTOR_DOC_TYPE);
        return getDetectorsFromElasticSearch(searchRequest);
    }

    @Override
    public List<ElasticsearchDetector> findByCreatedBy(String user) {
        val queryBuilder = QueryBuilders.matchQuery("createdBy", user);
        val searchSourceBuilder = elasticsearchUtil.getSourceBuilder(queryBuilder);
        val searchRequest = elasticsearchUtil.getSearchRequest(searchSourceBuilder, DETECTOR_INDEX, DETECTOR_DOC_TYPE);
        return getDetectorsFromElasticSearch(searchRequest);
    }

    @Override
    public void toggleDetector(String uuid, Boolean enabled) {
        val updateRequest = new UpdateRequest(DETECTOR_INDEX, DETECTOR_DOC_TYPE, uuid);
        updateRequest.doc("enabled", enabled);
        try {
            elasticSearchClient.update(updateRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(String.format("Updating elastic search failed", e));
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ElasticsearchDetector> getLastUpdatedDetectors(String fromDate, String toDate) {
        val queryBuilder = QueryBuilders.rangeQuery("lastUpdateTimestamp").from(fromDate).to(toDate);
        val searchSourceBuilder = elasticsearchUtil.getSourceBuilder(queryBuilder);
        val searchRequest = elasticsearchUtil.getSearchRequest(searchSourceBuilder, DETECTOR_INDEX, DETECTOR_DOC_TYPE);
        return getDetectorsFromElasticSearch(searchRequest);
    }


    private List<ElasticsearchDetector> getDetectorsFromElasticSearch(SearchRequest searchRequest) {
        SearchResponse response = new SearchResponse();
        try {
            response = elasticSearchClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("Error ES lookup", e);
            throw new RuntimeException(e);
        }

        SearchHit[] hits = response.getHits().getHits();
        List<ElasticsearchDetector> elasticsearchDetectors = new ArrayList<>();
        for (val hit : hits) {
            val elasticsearchDetector = (ElasticsearchDetector) objectMapperUtil.convertToObject(hit.getSourceAsString(), new TypeReference<ElasticsearchDetector>() {});
            val newElasticsearchDetector = getElasticSearchDetector(elasticsearchDetector);
            elasticsearchDetectors.add(newElasticsearchDetector);
        }
        return elasticsearchDetectors;
    }

    private ElasticsearchDetector getElasticSearchDetector(ElasticsearchDetector elasticsearchDetector) {
        return new ElasticsearchDetector()
                .setUuid(elasticsearchDetector.getUuid())
                .setCreatedBy(elasticsearchDetector.getCreatedBy())
                .setType(elasticsearchDetector.getType())
                .setDetectorConfig(elasticsearchDetector.getDetectorConfig())
                .setEnabled(elasticsearchDetector.getEnabled())
                .setLastUpdateTimestamp(elasticsearchDetector.getLastUpdateTimestamp());
    }

}
