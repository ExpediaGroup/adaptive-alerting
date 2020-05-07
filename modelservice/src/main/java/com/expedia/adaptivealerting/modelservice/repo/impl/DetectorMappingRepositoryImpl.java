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

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.expedia.adaptivealerting.modelservice.model.mapping.DetectorConsumerInfo;
import com.expedia.adaptivealerting.modelservice.exception.RecordNotFoundException;
import com.expedia.adaptivealerting.modelservice.model.percolator.PercolatorDetectorMapping;
import com.expedia.adaptivealerting.modelservice.elasticsearch.ElasticSearchClient;
import com.expedia.adaptivealerting.modelservice.elasticsearch.ElasticSearchProperties;
import com.expedia.adaptivealerting.modelservice.model.mapping.DetectorMapping;
import com.expedia.adaptivealerting.modelservice.repo.DetectorMappingRepository;
import com.expedia.adaptivealerting.modelservice.web.request.CreateDetectorMappingRequest;
import com.expedia.adaptivealerting.modelservice.web.request.SearchMappingsRequest;
import com.expedia.adaptivealerting.modelservice.web.response.DetectorMatchResponse;
import com.expedia.adaptivealerting.modelservice.web.response.MatchingDetectorsResponse;
import com.expedia.adaptivealerting.modelservice.util.ElasticsearchUtil;
import com.expedia.adaptivealerting.modelservice.util.ObjectMapperUtil;
import com.expedia.adaptivealerting.modelservice.util.QueryUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.percolator.PercolateQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.expedia.adaptivealerting.modelservice.model.percolator.PercolatorDetectorMapping.LAST_MOD_TIME_KEYWORD;

@Service
@Slf4j
public class DetectorMappingRepositoryImpl implements DetectorMappingRepository {

    private static final String DEFAULT_CONSUMER_ID = "ad-manager";

    @Autowired
    private ElasticSearchProperties elasticSearchProperties;

    @Autowired
    private ElasticSearchClient elasticSearchClient;

    @Autowired
    private ElasticsearchUtil elasticsearchUtil;

    @Autowired
    private ObjectMapperUtil objectMapperUtil;

    private final Timer delayTimer;
    private final Counter exceptionCount;

    @Autowired
    public DetectorMappingRepositoryImpl(MetricRegistry metricRegistry) {
        this.delayTimer = metricRegistry.timer("es-lookup.time-delay");
        this.exceptionCount = metricRegistry.counter("es-lookup.exception");
    }

    @Override
    public String createDetectorMapping(CreateDetectorMappingRequest request) {
        val indexName = elasticSearchProperties.getIndexName();
        val docType = elasticSearchProperties.getDocType();

        // Index mappings
        val fields = request.getFields();
        val newFieldMappings = elasticsearchUtil.removeFieldsHavingExistingMapping(fields, indexName, docType);
        elasticsearchUtil.updateIndexMappings(newFieldMappings, indexName, docType);

        // Index
        val indexRequest = new IndexRequest(indexName, docType);
        val now = System.currentTimeMillis();
        val mapping = new PercolatorDetectorMapping()
                .setUser(request.getUser())
                .setDetectorConsumerInfo(request.getDetectorConsumerInfo())
                .setQuery(QueryUtil.buildQuery(request.getExpression()))
                .setEnabled(true)
                .setLastModifiedTimeInMillis(now)
                .setCreatedTimeInMillis(now);
        val mappingJson = objectMapperUtil.convertToString(mapping);
        return elasticsearchUtil.index(indexRequest, mappingJson).getId();
    }

    @Override
    public MatchingDetectorsResponse findMatchingDetectorMappings(List<Map<String, String>> tagsList) {
        try {
            List<BytesReference> refList = new ArrayList<>();
            for (Map<String, String> tags : tagsList) {
                XContentBuilder xContent = XContentFactory.jsonBuilder();
                xContent.map(tags);
                refList.add(BytesReference.bytes(xContent));
            }
            PercolateQueryBuilder percolateQuery = new PercolateQueryBuilder(PercolatorDetectorMapping.QUERY_KEYWORD,
                    refList, XContentType.JSON);
            val termQuery = QueryBuilders.termQuery("aa_enabled", true);

            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            boolQueryBuilder.filter(percolateQuery);
            boolQueryBuilder.filter(termQuery);

            SearchSourceBuilder searchSourceBuilder = elasticsearchUtil.getSourceBuilder(boolQueryBuilder);
            searchSourceBuilder.timeout(new TimeValue(elasticSearchProperties.getConfig().getConnectionTimeout()));
            //FIXME setting default result set size to 500.
            // This is for returning detectors matching for set of metrics
            searchSourceBuilder.size(500);
            return getDetectorMappings(searchSourceBuilder, tagsList);
        } catch (IOException e) {
            log.error("Error ES lookup", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public DetectorMapping findDetectorMapping(String id) {
        val getRequest = new GetRequest(elasticSearchProperties.getIndexName(),
                elasticSearchProperties.getDocType(), id);
        try {
            val response = elasticSearchClient.get(getRequest, RequestOptions.DEFAULT);
            return getDetectorMapping(response.getSourceAsString(), response.getId(), Optional.empty());
        } catch (IOException e) {
            log.error(String.format("Get mapping %s failed", id), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<DetectorMapping> findLastUpdated(int timeInSeconds) {
        val sourceBuilder = new SearchSourceBuilder();
        val boolQuery = QueryBuilders.boolQuery();
        val fromTime = System.currentTimeMillis() - timeInSeconds * 1000;
        boolQuery.must(new RangeQueryBuilder(LAST_MOD_TIME_KEYWORD).gt(fromTime));
        sourceBuilder.query(boolQuery);
        //FIXME setting default result set size to 500.
        sourceBuilder.size(500);
        val searchRequest =
                new SearchRequest()
                        .source(sourceBuilder)
                        .indices(elasticSearchProperties.getIndexName())
                        .types(elasticSearchProperties.getDocType());
        return getDetectorMappings(searchRequest);
    }

    @Override
    public List<DetectorMapping> search(SearchMappingsRequest request) {
        val query = QueryBuilders.boolQuery();
        if (request.getUserId() != null) {
            query.must(userIdQuery(request));
        }
        if (request.getDetectorUuid() != null) {
            query.must(detectorIdQuery(request));
        }

        val searchSourceBuilder = elasticsearchUtil.getSourceBuilder(query);
        //FIXME setting default result set size to 500 until we have pagination.
        searchSourceBuilder.size(500);
        SearchRequest searchRequest = elasticsearchUtil.getSearchRequest(searchSourceBuilder, elasticSearchProperties.getIndexName(), elasticSearchProperties.getDocType());
        List<DetectorMapping> result = getDetectorMappings(searchRequest);
        //FIXME - move this condition to search query.
        return result.stream().filter(detectorMapping -> detectorMapping.isEnabled()).collect(Collectors.toList());
    }

    @Override
    public void disableDetectorMapping(String id) {
        val detectorMapping = findDetectorMapping(id);
        if (detectorMapping.isEnabled()) {
            final PercolatorDetectorMapping percolatorDetectorMapping = new PercolatorDetectorMapping()
                    .setUser(detectorMapping.getUser())
                    .setDetectorConsumerInfo(detectorMapping.getDetectorConsumerInfo())
                    .setQuery(QueryUtil.buildQuery(detectorMapping.getExpression()))
                    .setEnabled(false)
                    .setLastModifiedTimeInMillis(System.currentTimeMillis())
                    .setCreatedTimeInMillis(detectorMapping.getCreatedTimeInMillis());
            updateDetectorMapping(id, percolatorDetectorMapping);
        }
    }

    @Override
    public void deleteDetectorMapping(String id) {
        val deleteRequest = new DeleteRequest(elasticSearchProperties.getIndexName(), elasticSearchProperties.getDocType(), id);
        try {
            val deleteResponse = elasticSearchClient.delete(deleteRequest, RequestOptions.DEFAULT);
            if (elasticsearchUtil.checkNullResponse(deleteResponse.getResult())) {
                throw new RecordNotFoundException("Invalid request: " + id);
            }
        } catch (IOException e) {
            log.error(String.format("Deleting mapping %s failed", id), e);
            throw new RuntimeException(e);
        }
    }

    private List<DetectorMapping> getDetectorMappings(SearchRequest searchRequest) {
        try {
            val searchResponse = elasticSearchClient.search(searchRequest, RequestOptions.DEFAULT);
            val hits = searchResponse.getHits();
            return Arrays.asList(hits.getHits()).stream()
                    .map(hit -> getDetectorMapping(hit.getSourceAsString(), hit.getId(), Optional.empty()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Search failed", e);
            throw new RuntimeException("Search failed", e);
        }
    }

    private NestedQueryBuilder detectorIdQuery(SearchMappingsRequest searchMappingsRequest) {
        return QueryBuilders.nestedQuery(PercolatorDetectorMapping.DETECTOR_KEYWORD,
                QueryBuilders.boolQuery().must(QueryBuilders.matchPhraseQuery(
                        PercolatorDetectorMapping.DETECTOR_KEYWORD + "." + PercolatorDetectorMapping.DETECTOR_ID_KEYWORD,
                        searchMappingsRequest.getDetectorUuid().toString())),
                ScoreMode.None);
    }

    private NestedQueryBuilder userIdQuery(SearchMappingsRequest searchMappingsRequest) {
        return QueryBuilders.nestedQuery(PercolatorDetectorMapping.USER_KEYWORD,
                QueryBuilders.boolQuery().must(QueryBuilders.matchPhraseQuery(
                        PercolatorDetectorMapping.USER_KEYWORD + "." + PercolatorDetectorMapping.USER_ID_KEYWORD,
                        searchMappingsRequest.getUserId())),
                ScoreMode.None);
    }

    private MatchingDetectorsResponse getDetectorMappings(SearchSourceBuilder searchSourceBuilder,
                                                          List<Map<String, String>> tagsList) throws IOException {
        val searchRequest = new SearchRequest()
                .source(searchSourceBuilder)
                .indices(elasticSearchProperties.getIndexName());

        val searchResponse = elasticSearchClient.search(searchRequest, RequestOptions.DEFAULT);
        delayTimer.update(searchResponse.getTook().getMillis(), TimeUnit.MILLISECONDS);
        val hits = searchResponse.getHits().getHits();

        List<DetectorMapping> detectorMappings = Arrays.asList(hits).stream()
                .map(hit -> getDetectorMapping(hit.getSourceAsString(), hit.getId(), Optional.of(hit.getFields())))
                // .filter(detectorMapping -> detectorMapping.isEnabled()) //FIXME - move this condition into search query
                .collect(Collectors.toList());
        return convertToMatchingDetectorsResponse(new DetectorMatchResponse(detectorMappings,
                searchResponse.getTook().getMillis()));
    }

    private DetectorMapping getDetectorMapping(String json, String id, Optional<Map<String, DocumentField>> documentFieldMap) {
        val detectorEntity = (PercolatorDetectorMapping) objectMapperUtil.convertToObject(json, new TypeReference<PercolatorDetectorMapping>() {
        });
        val detectorMapping = new DetectorMapping()
                .setId(id)
                .setDetectorConsumerInfo(new DetectorConsumerInfo(getConsumerId(detectorEntity.getDetectorConsumerInfo().getConsumerId()), detectorEntity.getDetectorConsumerInfo().getUuid()))
                .setExpression(QueryUtil.buildExpression(detectorEntity.getQuery()))
                .setEnabled(detectorEntity.isEnabled())
                .setCreatedTimeInMillis(detectorEntity.getCreatedTimeInMillis())
                .setLastModifiedTimeInMillis(detectorEntity.getLastModifiedTimeInMillis())
                .setUser(detectorEntity.getUser());
        documentFieldMap.ifPresent(dfm -> {
            List values = dfm.get("_percolator_document_slot").getValues();
            List<Integer> indexes = new ArrayList<>();
            values.forEach(index -> {
                indexes.add(Integer.valueOf(index.toString()));
            });
            detectorMapping.setSearchIndexes(indexes);
        });
        return detectorMapping;
    }

    private MatchingDetectorsResponse convertToMatchingDetectorsResponse(DetectorMatchResponse res) {
        Map<Integer, List<DetectorConsumerInfo>> groupedDetectorsByIndex = new HashMap<>();
        log.info("Mapping-Cache: found {} matching mappings", res.getDetectorMappings().size());
        res.getDetectorMappings().forEach(detectorMapping -> {
            detectorMapping.getSearchIndexes().forEach(searchIndex -> {
                groupedDetectorsByIndex.computeIfAbsent(searchIndex, index -> new ArrayList<>());
                groupedDetectorsByIndex.computeIfPresent(searchIndex, (index, list) -> {
                    list.add(detectorMapping.getDetectorConsumerInfo());
                    return list;
                });
            });

        });
        return new MatchingDetectorsResponse(groupedDetectorsByIndex, res.getLookupTimeInMillis());
    }

    private void updateDetectorMapping(String index, PercolatorDetectorMapping percolatorDetectorMapping) {
        val indexRequest = new IndexRequest(elasticSearchProperties.getIndexName(), elasticSearchProperties.getDocType(), index);
        val json = objectMapperUtil.convertToString(percolatorDetectorMapping);
        elasticsearchUtil.index(indexRequest, json).getId();
    }

    //FIXME This is to prevent NULL consumerId for existing mappings.
    // This field will be made NON NULL once we update all the existing mappings to have consumerId
    private String getConsumerId(String consumerId) {
        return consumerId == null ? DEFAULT_CONSUMER_ID : consumerId;
    }
}
