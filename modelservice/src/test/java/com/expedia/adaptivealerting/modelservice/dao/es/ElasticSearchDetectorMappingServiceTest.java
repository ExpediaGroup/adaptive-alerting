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

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.expedia.adaptivealerting.modelservice.model.Detector;
import com.expedia.adaptivealerting.modelservice.model.MatchingDetectorsResponse;
import lombok.val;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ElasticSearchDetectorMappingServiceTest {
    @Mock
    private MetricRegistry metricRegistry;
    @Mock
    private ElasticSearchClient elasticSearchClient;
    @Mock
    private ElasticSearchConfig elasticSearchConfig;
    private ElasticSearchDetectorMappingService detectorMappingService;

    @Before
    public void beforeTest() {
        when(metricRegistry.timer(any())).thenReturn(mock(Timer.class));
        when(metricRegistry.counter(any())).thenReturn(mock(Counter.class));
        when(elasticSearchConfig.getIndexName()).thenReturn("detector-mappings");
        when(elasticSearchConfig.getDocType()).thenReturn("details");
        when(elasticSearchConfig.getConnectionTimeout()).thenReturn(100);
        detectorMappingService = new ElasticSearchDetectorMappingService(metricRegistry);
        ReflectionTestUtils.setField(detectorMappingService,"elasticSearchClient", elasticSearchClient);
        ReflectionTestUtils.setField(detectorMappingService,"elasticSearchConfig", elasticSearchConfig);
    }

    @Test
    public void findMatchingDetectorMappings_successful() throws IOException {
        List<Map<String, String>> tagsList = new ArrayList<>();
        Map<String, String> tags = new HashMap<>();
        tags.put("name", "sample-app");
        tags.put("env", "prod");
        tags.put("type", "gauge");
        val searchIndex = "2";
        val lookUpTime = 100;
        val detectorUuid = "aeb4d849-847a-45c0-8312-dc0fcf22b639";
        SearchResponse searchResponse = mockSearchResponse(searchIndex, lookUpTime, detectorUuid);
        when(elasticSearchClient.search(any(SearchRequest.class), eq(RequestOptions.DEFAULT))).thenReturn(searchResponse);
        MatchingDetectorsResponse response = detectorMappingService.findMatchingDetectorMappings(tagsList);
        verify(elasticSearchClient, atLeastOnce()).search(any(SearchRequest.class), eq(RequestOptions.DEFAULT));
        assertNotNull("Response can't be null", response);
        assertEquals("ES lookup time didn't match", lookUpTime, response.getLookupTimeInMillis());
        assertEquals(1, response.getGroupedDetectorsBySearchIndex().size());
        List<Detector> detectors = response.getGroupedDetectorsBySearchIndex().get(new Integer(searchIndex));
        assertEquals(1, detectors.size());
        assertEquals(UUID.fromString(detectorUuid), detectors.get(0).getId());
    }

    @Test(expected = RuntimeException.class)
    public void findMatchingDetectorMappings_fail() throws IOException {
        List<Map<String, String>> tagsList = new ArrayList<>();
        Map<String, String> tags = new HashMap<>();
        tags.put("name", "sample-app");
        tags.put("env", "prod");
        tags.put("type", "gauge");
        when(elasticSearchClient.search(any(SearchRequest.class), eq(RequestOptions.DEFAULT))).thenThrow(new IOException());
        detectorMappingService.findMatchingDetectorMappings(tagsList);
    }

    private SearchResponse mockSearchResponse(String searchIndex, int lookUpTime, String detectorUuid) {
        SearchResponse searchResponse = mock(SearchResponse.class);
        Map<String, DocumentField> fields =  new HashMap<>();
        fields.put("_percolator_document_slot", new DocumentField("_percolator_document_slot", Arrays.asList(new Integer(searchIndex))));
        SearchHit searchHit = new SearchHit(101, "xxx", null, fields);
        BytesReference source = new BytesArray("{\"aa_user\":{\"id\":\"test-user\"},\"aa_detector\":" +
                "{\"id\":\"" + detectorUuid + "\"},\"aa_query\":{\"bool\":{\"must\":[{\"match\":" +
                "{\"name\":\"sample-web\",\"env\":\"prod\"}}]}},\"aa_enabled\":true,\"aa_lastModifiedTime\":1554828886," +
                "\"aa_createdTime\":1554828886}\n");
        searchHit.sourceRef(source);
        SearchHit[] bunchOfSearchHits = new SearchHit[1];
        bunchOfSearchHits[0] = searchHit;
        SearchHits searchHits = new SearchHits(bunchOfSearchHits, 1, 1);
        when(searchResponse.getHits()).thenReturn(searchHits);
        TimeValue timeValue = new TimeValue(lookUpTime);
        when(searchResponse.getTook()).thenReturn(timeValue);
        return searchResponse;
    }
}
