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

import com.expedia.adaptivealerting.anomdetect.source.DetectorDocument;
import com.expedia.adaptivealerting.modelservice.repo.impl.elasticsearch.ElasticSearchClient;
import com.expedia.adaptivealerting.modelservice.repo.DetectorRepository;
import com.expedia.adaptivealerting.modelservice.util.DateUtil;
import com.expedia.adaptivealerting.modelservice.repo.impl.elasticsearch.ElasticsearchUtil;
import com.expedia.adaptivealerting.modelservice.util.ObjectMapperUtil;
import com.expedia.adaptivealerting.modelservice.util.RequestValidator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isNull;
import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

@Slf4j
@Service
public class DetectorRepositoryImpl implements DetectorRepository {
    private static final String DETECTOR_INDEX = "detectors";
    private static final String DETECTOR_DOC_TYPE = "detector";
    private static final int DEFAULT_ES_RESULTS_SIZE = 500;

    @Autowired
    private ElasticSearchClient elasticSearchClient;

    @Autowired
    private ObjectMapperUtil objectMapperUtil;

    @Autowired
    private ElasticsearchUtil elasticsearchUtil;

    @Override
    public UUID createDetector(DetectorDocument document) {
        notNull(document, "document can't be null");
        isNull(document.getUuid(), "Required: document.uuid == null");

        val uuid = UUID.randomUUID();
        document.setUuid(uuid);
        Date nowDate = DateUtil.now();

        // Force setting last update time to current time. Although deprecated, this field is still used to determine what changed for cache loading.
        // In the future this can change to the field within Meta
        document.setLastUpdateTimestamp(nowDate);

        // Set meta fields if none are provided
        if (document.getMeta() == null) {
            DetectorDocument.Meta metaBlock = new DetectorDocument.Meta();
            metaBlock.setDateCreated(nowDate);
            metaBlock.setCreatedBy(document.getCreatedBy());
            document.setMeta(metaBlock);
        } else {
            DetectorDocument.Meta metaBlock = document.getMeta();
            metaBlock.setDateCreated(nowDate);
            if (metaBlock.getCreatedBy() == null) {
                metaBlock.setCreatedBy(document.getCreatedBy());
            }
        }

        // Do this after setting the UUID since validation checks for the UUID.
        RequestValidator.validateDetector(document);

        val indexRequest = new IndexRequest(DETECTOR_INDEX, DETECTOR_DOC_TYPE, uuid.toString());
        val json = objectMapperUtil.convertToString(getElasticSearchDetector(document));

        // FIXME We should not be returning an implementation-specific ID here. (This is an Elasticsearch document ID.)
//        return elasticsearchUtil.index(indexRequest, json).getId();
        elasticsearchUtil.index(indexRequest, json);
        return uuid;
    }

    @Override
    public void deleteDetector(String uuid) {
        val deleteRequest = new DeleteRequest(DETECTOR_INDEX, DETECTOR_DOC_TYPE, uuid);
        try {
            val deleteResponse = elasticSearchClient.delete(deleteRequest, RequestOptions.DEFAULT);
            elasticsearchUtil.checkNullResponse(deleteResponse.getResult(), uuid);
        } catch (IOException e) {
            log.error(String.format("Deleting detector %s failed", uuid), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateDetector(String uuid, DetectorDocument document) {
        notNull(document, "document can't be null");
        RequestValidator.validateDetector(document);

        val updateRequest = new UpdateRequest(DETECTOR_INDEX, DETECTOR_DOC_TYPE, uuid);
        Map<String, Object> jsonMap = new HashMap<>();

        for (Field field : document.getClass().getDeclaredFields()) {
            //SAST SCAN. Access Specifier Manipulation. Using reflection utils to make the field accessible.
            ReflectionUtils.makeAccessible(field);
            String name = field.getName();
            if (!name.isEmpty()) {
                Object value;
                Date nowDate = DateUtil.now();
                try {
                    value = field.get(document);
                } catch (IllegalAccessException e) {
                    log.error(String.format("Updating elastic search failed", e));
                    throw new RuntimeException(e);
                }
                if ("lastUpdateTimestamp".equals(name)) {
                    value = DateUtil.toDateString(nowDate.toInstant());
                }
                if ("meta".equals(name)) {
                    // Set meta fields if none are provided
                    if (document.getMeta() == null) {
                        DetectorDocument.Meta metaBlock = new DetectorDocument.Meta();
                        metaBlock.setDateUpdated(nowDate);
                        document.setMeta(metaBlock);
                    } else {
                        DetectorDocument.Meta metaBlock = document.getMeta();
                        metaBlock.setDateUpdated(nowDate);
                        metaBlock.setUpdatedBy(document.getMeta().getUpdatedBy());
                    }
                }
                // Remap the JSON property name for any DetectorDocument properties with a defined JsonProperty name
                if (field.isAnnotationPresent(JsonProperty.class)) {
                    name = field.getAnnotation(JsonProperty.class).value();
                }
                if (value != null) {
                    jsonMap.put(name, value);
                }
            }
        }
        val json = objectMapperUtil.convertToString(jsonMap);   // Convert hashmap to JSON for elasticsearch
        updateRequest.doc(json, XContentType.JSON);
        try {
            val updateResponse = elasticSearchClient.update(updateRequest, RequestOptions.DEFAULT);
            log.info("updateResponse:{}", updateResponse.getResult());
            elasticsearchUtil.checkNullResponse(updateResponse.getResult(), uuid);
        } catch (IOException e) {
            log.error(String.format("Updating elastic search failed", e));
            throw new RuntimeException(e);
        }
    }

    @Override
    public DetectorDocument findByUuid(String uuid) {
        val queryBuilder = QueryBuilders.termQuery("uuid", uuid);
        val searchSourceBuilder = elasticsearchUtil.getSourceBuilder(queryBuilder).size(DEFAULT_ES_RESULTS_SIZE);
        val searchRequest = elasticsearchUtil.getSearchRequest(searchSourceBuilder, DETECTOR_INDEX, DETECTOR_DOC_TYPE);
        return getDetectorsFromElasticSearch(searchRequest).get(0);
    }

    @Override
    public List<DetectorDocument> findByCreatedBy(String user) {
        val queryBuilder = QueryBuilders.termQuery("createdBy", user);
        val searchSourceBuilder = elasticsearchUtil.getSourceBuilder(queryBuilder).size(DEFAULT_ES_RESULTS_SIZE);
        val searchRequest = elasticsearchUtil.getSearchRequest(searchSourceBuilder, DETECTOR_INDEX, DETECTOR_DOC_TYPE);
        return getDetectorsFromElasticSearch(searchRequest);
    }

    @Override
    public void toggleDetector(String uuid, Boolean enabled) {
        val updateRequest = new UpdateRequest(DETECTOR_INDEX, DETECTOR_DOC_TYPE, uuid);

        Date nowDate = DateUtil.now();
        String nowValue = DateUtil.toDateString(nowDate.toInstant());

        Map<String, Object> jsonMap = new HashMap<>();
        Map<String, Object> metaMap = new HashMap<>();
        metaMap.put("dateUpdated", nowDate);
        jsonMap.put("lastUpdateTimestamp", nowValue);
        jsonMap.put("enabled", enabled);
        jsonMap.put("meta", metaMap);
        updateRequest.doc(jsonMap);
        try {
            val updateResponse = elasticSearchClient.update(updateRequest, RequestOptions.DEFAULT);
            elasticsearchUtil.checkNullResponse(updateResponse.getResult(), uuid);

        } catch (IOException e) {
            log.error(String.format("Updating elastic search failed", e));
            throw new RuntimeException(e);
        }
    }

    @Override
    public void trustDetector(String uuid, Boolean trusted) {
        val updateRequest = new UpdateRequest(DETECTOR_INDEX, DETECTOR_DOC_TYPE, uuid);

        Date nowDate = DateUtil.now();
        String nowValue = DateUtil.toDateString(nowDate.toInstant());

        Map<String, Object> jsonMap = new HashMap<>();
        Map<String, Object> metaMap = new HashMap<>();
        metaMap.put("dateUpdated", nowDate);
        jsonMap.put("lastUpdateTimestamp", nowValue);
        jsonMap.put("trusted", trusted);
        jsonMap.put("meta", metaMap);
        updateRequest.doc(jsonMap);
        try {
            val updateResponse = elasticSearchClient.update(updateRequest, RequestOptions.DEFAULT);
            elasticsearchUtil.checkNullResponse(updateResponse.getResult(), uuid);
        } catch (IOException e) {
            log.error(String.format("Updating elastic search failed", e));
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<DetectorDocument> getLastUpdatedDetectors(long interval) {
        // Replaced Lombok val with explicit types here because the Maven compiler plugin was breaking under
        // OpenJDK 12. Not sure what the issue was but this fixed it. [WLW]
        Instant now = DateUtil.now().toInstant();
        String fromDate = DateUtil.toUtcDateString((now.minus(interval, ChronoUnit.SECONDS)));
        String toDate = DateUtil.toUtcDateString(now);
        return getLastUpdatedDetectors(fromDate, toDate);
    }

    @Override
    public List<DetectorDocument> getLastUpdatedDetectors(String fromDate, String toDate) {
        val queryBuilder = QueryBuilders.rangeQuery("lastUpdateTimestamp").from(fromDate).to(toDate);
        val searchSourceBuilder = elasticsearchUtil.getSourceBuilder(queryBuilder).size(DEFAULT_ES_RESULTS_SIZE);
        val searchRequest = elasticsearchUtil.getSearchRequest(searchSourceBuilder, DETECTOR_INDEX, DETECTOR_DOC_TYPE);
        return getDetectorsFromElasticSearch(searchRequest);
    }

    private List<DetectorDocument> getDetectorsFromElasticSearch(SearchRequest searchRequest) {
        SearchResponse response;
        try {
            response = elasticSearchClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("Error ES lookup", e);
            throw new RuntimeException(e);
        }

        SearchHit[] hits = response.getHits().getHits();
        List<DetectorDocument> detectors = new ArrayList<>();
        for (val hit : hits) {

            val detector = (DetectorDocument) objectMapperUtil.convertToObject(hit.getSourceAsString(), new TypeReference<DetectorDocument>() {
            });
            val newElasticsearchDetector = getElasticSearchDetector(detector);
            detectors.add(newElasticsearchDetector);
        }
        return detectors;
    }

    private DetectorDocument getElasticSearchDetector(DetectorDocument detector) {
        return new DetectorDocument()
                .setUuid(detector.getUuid())
                .setCreatedBy(detector.getCreatedBy())
                .setType(detector.getType())
                .setConfig(detector.getConfig())
                .setMeta(detector.getMeta())
                .setEnabled(detector.isEnabled())
                .setTrusted(detector.isTrusted())
                .setLastUpdateTimestamp(detector.getLastUpdateTimestamp());
    }

}
