package com.expedia.adaptivealerting.modelservice.detectormapper.service.es;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.expedia.adaptivealerting.modelservice.detectormapper.service.*;
import com.expedia.adaptivealerting.modelservice.detectormapper.util.QueryUtil;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.ConstantScoreQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.percolator.PercolateQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.expedia.adaptivealerting.modelservice.detectormapper.service.es.DetectorMappingEntity.LAST_MOD_TIME_KEYWORD;

@Component
@Slf4j
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.AvoidThrowingRawExceptionTypes"})
public class ElasticSearchDetectorMappingService implements DetectorMappingService {
    private static Gson gson = new Gson();

    @Autowired
    private ElasticSearchConfig elasticSearchConfig;

    @Autowired
    private RestHighLevelClient client;

    private final Timer delayTimer;
    private final Counter exceptionCount;

    @Autowired
    public ElasticSearchDetectorMappingService( MetricRegistry metricRegistry) {
        this.delayTimer = metricRegistry.timer("es-lookup.time-delay");
        this.exceptionCount = metricRegistry.counter("es-lookup.exception");
    }

    @Override
    public MatchingDetectorsResponse findMatchingDetectorMappings(List<Map<String, String>> tagsList) {
        try {
            List<BytesReference> refList = new ArrayList<>();

            for (Map<String, String> tags: tagsList) {
                XContentBuilder xContent = XContentFactory.jsonBuilder();
                xContent.map(tags);
                refList.add(BytesReference.bytes(xContent));
            }

            PercolateQueryBuilder percolateQuery =
                new PercolateQueryBuilder(DetectorMappingEntity.QUERY_KEYWORD, elasticSearchConfig.getDocType(),
                    refList, XContentType.JSON);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            ConstantScoreQueryBuilder constantScoreQueryBuilder = new ConstantScoreQueryBuilder(percolateQuery);
            log.info("Percolator query: {}", constantScoreQueryBuilder.toString());
            searchSourceBuilder.query(constantScoreQueryBuilder);
            searchSourceBuilder.timeout(new TimeValue(elasticSearchConfig.getConnectionTimeout()));
            //FIXME setting default result set size to 500.
            searchSourceBuilder.size(500);
            return getDetectorMappings(client, searchSourceBuilder, tagsList);
        } catch (IOException e) {
            this.exceptionCount.inc();
            log.error("Error ES lookup", e);
            return null;
        }
    }

    @Override
    public String createDetectorMapping(CreateDetectorMappingRequest createRequest) {
    
        Set<String> fields = getFields(createRequest);
        Set<String> newFieldMappings = getFieldsWithoutExistingMapping(fields);
        updateIndexMappings(newFieldMappings);
        
        final DetectorMappingEntity detectorMappingEntity = new DetectorMappingEntity(createRequest.getUser(),
            createRequest.getDetector(), QueryUtil.buildQuery(createRequest.getConditionExpression()), true,
            System.currentTimeMillis(), System.currentTimeMillis());
        final IndexRequest indexRequest = new IndexRequest(elasticSearchConfig.getIndexName(),
            elasticSearchConfig.getDocType());
        indexRequest.source(gson.toJson(detectorMappingEntity), XContentType.JSON);
        try {
            IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
            return indexResponse.getId();
        } catch (IOException e) {
            log.error(String.format("Indexing mapping %s failed", gson.toJson(detectorMappingEntity)), e);
            throw new RuntimeException(e);
        }
    }
    
    private void updateIndexMappings(Set<String> newFieldMappings) {
        PutMappingRequest request = new PutMappingRequest(elasticSearchConfig.getIndexName());
        Map<String, Object> jsonMap = new HashMap<>();
        Map<String, Object> type = new HashMap<>();
        type.put("type", "keyword");
        Map<String, Object> properties = new HashMap<>();
        newFieldMappings.forEach(field -> {
            properties.put(field, type);
        });
        jsonMap.put("properties", properties);
        request.source(jsonMap);
        request.type(elasticSearchConfig.getDocType());
        try {
            client.indices().putMapping(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("Error updating mappings", e);
            throw new RuntimeException(e);
        }
    }
    
    private Set<String> getFieldsWithoutExistingMapping(Set<String> fields) {
        GetMappingsRequest request = new GetMappingsRequest();
        request.indices(elasticSearchConfig.getIndexName());
        
        try {
            GetMappingsResponse mappingsResponse = client.indices().getMapping(request, RequestOptions.DEFAULT);
            Set<String> mappeddFields = ((Map<String, String>)mappingsResponse.getMappings()
                    .get(elasticSearchConfig.getIndexName())
                    .get(elasticSearchConfig.getDocType()).sourceAsMap().get("properties")).entrySet().stream()
                    .map(en -> en.getKey()).collect(Collectors.toSet());
            fields.removeAll(mappeddFields);
            return fields;
        } catch (IOException e) {
            log.error("Error finding mappings", e);
            throw new RuntimeException(e);
        }
    }
    
    private Set<String> getFields(CreateDetectorMappingRequest createRequest) {
        return createRequest.getConditionExpression().getOperands().stream()
                .map(operand -> operand.getField().getKey()).collect(Collectors.toSet());
    }
    
    @Override
    public void updateDetectorMapping(UpdateDetectorMappingRequest updateDetectorMappingRequest) {
        //TODO
    }
    
    private void updateDetectorMapping(String index, DetectorMappingEntity detectorMappingEntity) {
        final IndexRequest indexRequest = new IndexRequest(elasticSearchConfig.getIndexName(),
                elasticSearchConfig.getDocType(), index);
        indexRequest.source(gson.toJson(detectorMappingEntity), XContentType.JSON);
        try {
            client.index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("Update Index failed", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteDetectorMapping(String id) {
        final DeleteRequest deleteRequest = new DeleteRequest(elasticSearchConfig.getIndexName(),
                elasticSearchConfig.getDocType(), id);
        try {
            client.delete(deleteRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(String.format("Deleting mapping %s failed", id), e);
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void disableDetectorMapping(String id) {
        DetectorMapping detectorMapping = findDetectorMapping(id);
        if (detectorMapping.isEnabled()) {
            final DetectorMappingEntity detectorMappingEntity = new DetectorMappingEntity(detectorMapping.getUser(),
                    detectorMapping.getDetector(), QueryUtil.buildQuery(detectorMapping.getConditionExpression()),
                    false,
                    System.currentTimeMillis(), detectorMapping.getCreatedTimeInMillis());
            updateDetectorMapping(id, detectorMappingEntity);
        }
    }

    @Override
    public DetectorMapping findDetectorMapping(String id) {
        GetRequest getRequest = new GetRequest(elasticSearchConfig.getIndexName(),
            elasticSearchConfig.getDocType(),
            id);
        try {
            GetResponse response = client.get(getRequest, RequestOptions.DEFAULT);
            return getDetectorMapping(response.getSourceAsString(), response.getId(), Optional.empty());
        } catch (IOException e) {
            log.error(String.format("Get mapping %s failed", id), e);
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public List<DetectorMapping> search(SearchMappingsRequest searchMappingsRequest) {
        SearchRequest searchRequest = new SearchRequest(elasticSearchConfig.getIndexName());
        searchRequest.types(elasticSearchConfig.getDocType());
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        if (searchMappingsRequest.getUserId() != null) {
            query.must(userIdQuery(searchMappingsRequest));
        }
        if (searchMappingsRequest.getDetectorUuid() != null) {
            query.must(detectorIdQuery(searchMappingsRequest));
        }
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(query);
        
        //FIXME setting default result set size to 10000 until we have pagination.
        searchSourceBuilder.size(10000);
        searchRequest.source(searchSourceBuilder);
        List<DetectorMapping> result = getDetectorMappings(searchRequest);
        //FIXME - move this condition to search query.
        return result.stream().filter(detectorMapping -> detectorMapping.isEnabled()).collect(Collectors.toList());
    }
    
    @Override
    public List<DetectorMapping> findLastUpdated(int timeInSeconds) {
        final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
    
        final BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        long fromTime = System.currentTimeMillis() - timeInSeconds * 1000;
        boolQuery.must(new RangeQueryBuilder(LAST_MOD_TIME_KEYWORD).gt(fromTime));
        sourceBuilder.query(boolQuery);
        //FIXME setting default result set size to 500.
        sourceBuilder.size(500);
    
        final SearchRequest searchRequest =
                new SearchRequest()
                        .source(sourceBuilder)
                        .indices(elasticSearchConfig.getIndexName())
                        .types(elasticSearchConfig.getDocType());
        return getDetectorMappings(searchRequest);
    }
    
    private List<DetectorMapping> getDetectorMappings(SearchRequest searchRequest) {
        try {
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse.getHits();
            return Arrays.asList(hits.getHits()).stream()
                    .map(hit -> getDetectorMapping(hit.getSourceAsString(), hit.getId(), Optional.empty()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Search failed", e);
            throw new RuntimeException("Search failed", e);
        }
    }
    
    private NestedQueryBuilder detectorIdQuery(SearchMappingsRequest searchMappingsRequest) {
        return QueryBuilders.nestedQuery(DetectorMappingEntity.DETECTOR_KEYWORD,
        QueryBuilders.boolQuery().must(QueryBuilders.matchPhraseQuery(
                DetectorMappingEntity.DETECTOR_KEYWORD + "." + DetectorMappingEntity.DETECTOR_ID_KEYWORD,
                searchMappingsRequest.getDetectorUuid().toString())),
        ScoreMode.None);
    }
    
    private NestedQueryBuilder userIdQuery(SearchMappingsRequest searchMappingsRequest) {
        return QueryBuilders.nestedQuery(DetectorMappingEntity.USER_KEYWORD,
            QueryBuilders.boolQuery().must(QueryBuilders.matchPhraseQuery(
                    DetectorMappingEntity.USER_KEYWORD + "." + DetectorMappingEntity.USER_ID_KEYWORD,
                    searchMappingsRequest.getUserId())),
            ScoreMode.None);
    }
    
    private MatchingDetectorsResponse getDetectorMappings(RestHighLevelClient client,
                                                      SearchSourceBuilder searchSourceBuilder,
                                                      List<Map<String, String>> tagsList) {
        final SearchRequest searchRequest =
            new SearchRequest()
                .source(searchSourceBuilder)
                .indices(elasticSearchConfig.getIndexName());
        try {
            log.info("ES lookup start for {}", tagsList.toArray());
            SearchResponse searchResponse = client.search(searchRequest);
            log.info("search Request {}", searchRequest.toString());
            log.info("ES took {} for {}", searchResponse.getTook().getMillis(), tagsList.size());
            delayTimer.update(searchResponse.getTook().getMillis(), TimeUnit.MILLISECONDS);
            SearchHit hits[] = searchResponse.getHits().getHits();
         //   return Arrays.asList(hits.getHits()).stream()
            //        .map(hit -> getDetectorMapping(hit.getSourceAsString(), hit.getId(), Optional.empty()))
            //        .collect(Collectors.toList());
            List<DetectorMapping> detectorMappings = Arrays.asList(hits).stream()
                .map(hit -> getDetectorMapping(hit.getSourceAsString(), hit.getId(), Optional.of(hit.getFields())))
               // .filter(detectorMapping -> detectorMapping.isEnabled()) //FIXME - move this condition into search query
                .collect(Collectors.toList());
            log.info("ES detectormappings hits as received {}", detectorMappings.size());
            return ConverttoMatchingDetectorsResponse(new DetectorMatchResponse(detectorMappings, searchResponse.getTook().getMillis()));
        } catch (IOException e) {
            log.error("ES search failed", e);
            this.exceptionCount.inc();
        }
        return null;
    }

    private DetectorMapping getDetectorMapping(String json, String id, Optional<Map<String, DocumentField>> documentFieldMap) {
        DetectorMappingEntity detectorEntity = gson.fromJson(json, DetectorMappingEntity.class);
        DetectorMapping detectorMapping = new DetectorMapping();
        detectorMapping.setId(id);
        detectorMapping.setDetector(new Detector(detectorEntity.getDetector().getId()));
        detectorMapping.setConditionExpression(QueryUtil.buildExpressionTree(detectorEntity.getQuery()));
        detectorMapping.setEnabled(detectorEntity.isEnabled());
        detectorMapping.setCreatedTimeInMillis(detectorEntity.getCreatedTimeInMillis());
        detectorMapping.setLastModifiedTimeInMillis(detectorEntity.getLastModifiedTimeInMillis());
        detectorMapping.setUser(detectorEntity.getUser());
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

    private MatchingDetectorsResponse ConverttoMatchingDetectorsResponse(DetectorMatchResponse res) {
        Map<Integer, List<Detector>> groupedDetectorsByIndex = new HashMap<>();
        log.info("Mapping-Cache: found {} matching mappings", res.getDetectorMappings().size());
        res.getDetectorMappings().forEach(detectorMapping -> {
            detectorMapping.getSearchIndexes().forEach(searchIndex -> {
                groupedDetectorsByIndex.computeIfAbsent(searchIndex, index -> new ArrayList<>());
                groupedDetectorsByIndex.computeIfPresent(searchIndex, (index, list) -> {
                    list.add(detectorMapping.getDetector());
                    return list;
                });
            });

        });
        return new MatchingDetectorsResponse(groupedDetectorsByIndex, res.getLookupTimeInMillis());
    }
}
