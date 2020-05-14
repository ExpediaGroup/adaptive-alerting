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

import com.expedia.adaptivealerting.modelservice.domain.mapping.Expression;
import com.expedia.adaptivealerting.modelservice.elasticsearch.LegacyElasticSearchClient;
import com.expedia.adaptivealerting.modelservice.elasticsearch.ElasticSearchProperties;
import com.expedia.adaptivealerting.modelservice.repo.MetricProfileRepository;
import com.expedia.adaptivealerting.modelservice.web.request.CreateMetricProfilingRequest;
import com.expedia.adaptivealerting.modelservice.test.ObjectMother;
import com.expedia.adaptivealerting.modelservice.elasticsearch.ElasticsearchUtil;
import com.expedia.adaptivealerting.modelservice.util.ObjectMapperUtil;
import lombok.SneakyThrows;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MetricProfileRepositoryTest {

    @Spy
    @InjectMocks
    private MetricProfileRepository repository = new MetricProfileRepositoryImpl();

    @Mock
    private LegacyElasticSearchClient legacyElasticSearchClient;

    @Mock
    private ElasticsearchUtil elasticsearchUtil;

    @Mock
    private ObjectMapperUtil objectMapperUtil;

    @Mock
    private ElasticSearchProperties elasticSearchProperties;

    private IndexResponse indexResponse;

    private SearchResponse searchResponse;

    private Expression expression;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
    }

    private void initTestObjects() {
        ObjectMother mom = ObjectMother.instance();
        indexResponse = mockIndexResponse();
        searchResponse = mockSearchResponse("1");
        this.expression = mom.getExpression();

    }

    @SneakyThrows
    private void initDependencies() {
        Mockito.when(elasticsearchUtil.getSourceBuilder(any(QueryBuilder.class))).thenReturn(new SearchSourceBuilder());
        Mockito.when(elasticsearchUtil.index(any(IndexRequest.class), anyString())).thenReturn(indexResponse);

        Mockito.when(objectMapperUtil.convertToString(any())).thenReturn("");
        Mockito.when(legacyElasticSearchClient.search(any(SearchRequest.class), any(RequestOptions.class))).thenReturn(searchResponse);
        Mockito.when(legacyElasticSearchClient.update(any(UpdateRequest.class), any(RequestOptions.class))).thenReturn(new UpdateResponse());

        ElasticSearchProperties.Config config = new ElasticSearchProperties.Config().setConnectionTimeout(100);
        Mockito.when(elasticSearchProperties.getConfig()).thenReturn(config);
    }

    @Test
    public void testCreateMetricProfile() {
        CreateMetricProfilingRequest request = new CreateMetricProfilingRequest();
        request.setExpression(expression);
        request.setIsStationary(true);

        String actualCreateId = repository.createMetricProfile(request);
        assertNotNull(actualCreateId);
        assertEquals("1", actualCreateId);
    }

    @Test
    public void testUpdateMetricProfile() {
        repository.updateMetricProfile("id", true);
        verify(repository, times(1)).updateMetricProfile("id", true);
    }

    @Test
    public void testFindMatchingMetricProfiles() {
        boolean profilingExists = repository.profilingExists(new HashMap<>());
        assertNotNull(profilingExists);
        assertEquals(true, profilingExists);
    }

    @Test(expected = RuntimeException.class)
    public void updateMetricProfileFail() throws IOException {
        Mockito.when(legacyElasticSearchClient.update(any(UpdateRequest.class), any(RequestOptions.class))).thenThrow(new IOException());
        repository.updateMetricProfile("id", true);
    }

    @Test(expected = RuntimeException.class)
    public void findMatchingMetricProfilesFail() throws IOException {
        Mockito.when(legacyElasticSearchClient.search(any(SearchRequest.class), any(RequestOptions.class))).thenThrow(new IOException());
        repository.profilingExists(new HashMap<>());
    }

    private IndexResponse mockIndexResponse() {
        IndexResponse indexResponse = mock(IndexResponse.class);
        when(indexResponse.getId()).thenReturn("1");
        return indexResponse;
    }

    private SearchResponse mockSearchResponse(String searchIndex) {
        SearchResponse searchResponse = mock(SearchResponse.class);
        Map<String, DocumentField> fields = new HashMap<>();
        SearchHit searchHit = new SearchHit(101, "xxx", null, fields);
        BytesReference source = new BytesArray("{}");
        searchHit.sourceRef(source);
        SearchHit[] bunchOfSearchHits = new SearchHit[1];
        bunchOfSearchHits[0] = searchHit;
        SearchHits searchHits = new SearchHits(bunchOfSearchHits, 1, 1);
        when(searchResponse.getHits()).thenReturn(searchHits);
        return searchResponse;
    }
}
