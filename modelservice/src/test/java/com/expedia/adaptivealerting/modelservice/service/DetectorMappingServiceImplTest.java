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

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.expedia.adaptivealerting.modelservice.dto.detectormapping.Detector;
import com.expedia.adaptivealerting.modelservice.dto.detectormapping.DetectorMapping;
import com.expedia.adaptivealerting.modelservice.dto.detectormapping.MatchingDetectorsResponse;
import com.expedia.adaptivealerting.modelservice.dto.detectormapping.SearchMappingsRequest;
import com.expedia.adaptivealerting.modelservice.elasticsearch.ElasticSearchClient;
import com.expedia.adaptivealerting.modelservice.service.DetectorMappingServiceImpl;
import com.expedia.adaptivealerting.modelservice.elasticsearch.ElasticSearchProperties;
import lombok.val;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.io.stream.ByteBufferStreamInput;
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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DetectorMappingServiceImplTest {
    @Mock
    private MetricRegistry metricRegistry;
    @Mock
    private ElasticSearchClient elasticSearchClient;
    @Mock
    private ElasticSearchProperties elasticSearchProperties;
    private DetectorMappingServiceImpl detectorMappingService;

    @Before
    public void beforeTest() {
        when(metricRegistry.timer(any())).thenReturn(mock(Timer.class));
        when(metricRegistry.counter(any())).thenReturn(mock(Counter.class));
        when(elasticSearchProperties.getIndexName()).thenReturn("detector-mappings");
        when(elasticSearchProperties.getDocType()).thenReturn("details");
        ElasticSearchProperties.Config config = new ElasticSearchProperties.Config()
                .setConnectionTimeout(100);
        when(elasticSearchProperties.getConfig()).thenReturn(config);
        detectorMappingService = new DetectorMappingServiceImpl(metricRegistry);
        ReflectionTestUtils.setField(detectorMappingService, "elasticSearchClient", elasticSearchClient);
        ReflectionTestUtils.setField(detectorMappingService, "elasticSearchProperties", elasticSearchProperties);
    }

    @Test
    public void findMatchingDetectorMappings_successful() throws IOException {
        List<Map<String, String>> tagsList = new ArrayList<>();
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
        when(elasticSearchClient.search(any(SearchRequest.class), eq(RequestOptions.DEFAULT))).thenThrow(new IOException());
        detectorMappingService.findMatchingDetectorMappings(tagsList);
    }

    @Test
    public void findDetectorMapping_successful() throws IOException {
        List<Map<String, String>> tagsList = new ArrayList<>();
        val detectorUuid = "aeb4d849-847a-45c0-8312-dc0fcf22b639";
        String id = "adsvade8^szx";
        Long LastModifiedTimeInMillis = new Long(1554828886);
        Long CreatedTimeInMillis = new Long(1554828886);
        GetResponse getResponse = mockGetResponse(id);
        when(elasticSearchClient.get(any(GetRequest.class), eq(RequestOptions.DEFAULT))).thenReturn(getResponse);
        DetectorMapping detectorMapping = detectorMappingService.findDetectorMapping(id);
        verify(elasticSearchClient, atLeastOnce()).get(any(GetRequest.class), eq(RequestOptions.DEFAULT));
        assertNotNull("Response can't be null", detectorMapping);
        assertEquals(id, detectorMapping.getId());
        assertEquals("test-user", detectorMapping.getUser().getId());
        assertEquals(LastModifiedTimeInMillis, Long.valueOf(detectorMapping.getLastModifiedTimeInMillis()));
        assertEquals(CreatedTimeInMillis, Long.valueOf(detectorMapping.getCreatedTimeInMillis()));
        assertTrue(detectorMapping.isEnabled());
        assertEquals(UUID.fromString(detectorUuid), detectorMapping.getDetector().getId());

    }

    @Test(expected = RuntimeException.class)
    public void findDetectorMapping_fail() throws IOException {
        String id = "adsvade8^szx";
        when(elasticSearchClient.get(any(GetRequest.class), eq(RequestOptions.DEFAULT))).thenThrow(new IOException());
        detectorMappingService.findDetectorMapping(id);
    }

    @Test(expected = RuntimeException.class)
    public void findMatchingDetectorMappings_tagsListforloopcheck() throws IOException {
        List<Map<String, String>> tagsList = new ArrayList<>();
        Map<String, String> tags = new HashMap<>();
        tags.put("name", "sample-app");
        tags.put("env", "prod");
        tags.put("type", "gauge");
        tagsList.add(tags);
        when(elasticSearchClient.search(any(SearchRequest.class), eq(RequestOptions.DEFAULT))).thenThrow(new IOException());
        detectorMappingService.findMatchingDetectorMappings(tagsList);
    }

    @Test
    public void findLastUpdated_successful() throws IOException {
        List<DetectorMapping> tagsList = new ArrayList<>();
        Map<String, String> tags = new HashMap<>();
        Long LastModifiedTimeInMillis = new Long(1554828886);
        Long CreatedTimeInMillis = new Long(1554828886);
        val searchIndex = "2";
        val lookUpTime = 100;
        val detectorUuid = "aeb4d849-847a-45c0-8312-dc0fcf22b639";
        int TimeinSeconds = 60;
        SearchResponse searchResponse = mockSearchResponse(searchIndex, lookUpTime, detectorUuid);
        when(elasticSearchClient.search(any(SearchRequest.class), eq(RequestOptions.DEFAULT))).thenReturn(searchResponse);
        tagsList = detectorMappingService.findLastUpdated(TimeinSeconds);
        verify(elasticSearchClient, atLeastOnce()).search(any(SearchRequest.class), eq(RequestOptions.DEFAULT));
        assertNotNull("Response can't be null", tagsList);
        assertEquals(1, tagsList.size());
        assertEquals(UUID.fromString(detectorUuid), tagsList.get(0).getDetector().getId());
        assertEquals("test-user", tagsList.get(0).getUser().getId());
        assertEquals(LastModifiedTimeInMillis, Long.valueOf(tagsList.get(0).getLastModifiedTimeInMillis()));
        assertEquals(CreatedTimeInMillis, Long.valueOf(tagsList.get(0).getCreatedTimeInMillis()));
        assertTrue(tagsList.get(0).isEnabled());
    }

    @Test(expected = RuntimeException.class)
    public void findLastUpdated_fail() throws IOException {
        int TimeinSeconds = 60;
        when(elasticSearchClient.search(any(SearchRequest.class), eq(RequestOptions.DEFAULT))).thenThrow(new IOException());
        detectorMappingService.findLastUpdated(TimeinSeconds);
    }

    @Test
    public void search_successful() throws IOException {
        List<DetectorMapping> tagsList = new ArrayList<>();
        Map<String, String> tags = new HashMap<>();
        Long LastModifiedTimeInMillis = new Long(1554828886);
        Long CreatedTimeInMillis = new Long(1554828886);
        val searchIndex = "2";
        val lookUpTime = 100;
        val detectorUuid = "aeb4d849-847a-45c0-8312-dc0fcf22b639";
        SearchMappingsRequest searchMappingsRequest = new SearchMappingsRequest();
        SearchResponse searchResponse = mockSearchResponse(searchIndex, lookUpTime, detectorUuid);
        when(elasticSearchClient.search(any(SearchRequest.class), eq(RequestOptions.DEFAULT))).thenReturn(searchResponse);
        tagsList = detectorMappingService.search(searchMappingsRequest);
        verify(elasticSearchClient, atLeastOnce()).search(any(SearchRequest.class), eq(RequestOptions.DEFAULT));
        assertNotNull("Response can't be null", tagsList);
        assertEquals(1, tagsList.size());
        assertEquals(UUID.fromString(detectorUuid), tagsList.get(0).getDetector().getId());
        assertEquals("test-user", tagsList.get(0).getUser().getId());
        assertEquals(LastModifiedTimeInMillis, Long.valueOf(tagsList.get(0).getLastModifiedTimeInMillis()));
        assertEquals(CreatedTimeInMillis, Long.valueOf(tagsList.get(0).getCreatedTimeInMillis()));
        assertTrue(tagsList.get(0).isEnabled());
    }

    @Test(expected = RuntimeException.class)
    public void search_conditionalbranches() throws IOException {
        SearchMappingsRequest searchMappingsRequest = new SearchMappingsRequest();
        searchMappingsRequest.setUserId("test-user");
        searchMappingsRequest.setDetectorUuid(UUID.fromString("aeb4d849-847a-45c0-8312-dc0fcf22b639"));
        when(elasticSearchClient.search(any(SearchRequest.class), eq(RequestOptions.DEFAULT))).thenThrow(new IOException());
        detectorMappingService.search(searchMappingsRequest);
    }

    @Test(expected = RuntimeException.class)
    public void search_fail() throws IOException {
        int TimeinSeconds = 60;
        when(elasticSearchClient.search(any(SearchRequest.class), eq(RequestOptions.DEFAULT))).thenThrow(new IOException());
        detectorMappingService.findLastUpdated(TimeinSeconds);
    }

    @Test
    public void disableDetectorMapping() throws IOException {
        val id = "adsvade8^szx";
        GetResponse getResponse = mockGetResponse(id);
        when(elasticSearchClient.get(any(GetRequest.class), eq(RequestOptions.DEFAULT))).thenReturn(getResponse);
        detectorMappingService.disableDetectorMapping(id);
        verify(elasticSearchClient, atLeastOnce()).get(any(GetRequest.class), eq(RequestOptions.DEFAULT));
    }

    @Test
    public void deleteDetectorMapping_successful() throws Exception {
        val id = "adsvade8^szx";
        DeleteResponse deleteResponse = mockDeleteResponse(id);
        when(elasticSearchClient.delete(any(DeleteRequest.class), eq(RequestOptions.DEFAULT))).thenReturn(new DeleteResponse());
        detectorMappingService.deleteDetectorMapping(id);
        verify(elasticSearchClient, atLeastOnce()).delete(any(DeleteRequest.class), eq(RequestOptions.DEFAULT));
        assertEquals(id, deleteResponse.getId());
        assertEquals(elasticSearchProperties.getIndexName(), deleteResponse.getIndex());
        assertEquals("DELETED", deleteResponse.getResult().toString());
    }

    @Test(expected = RuntimeException.class)
    public void deleteDetectorMapping_fail() throws IOException {
        val id = "adsvade8^szx";
        DeleteRequest deleteRequest = new DeleteRequest(elasticSearchProperties.getIndexName(),
                elasticSearchProperties.getDocType(), id);
        when(elasticSearchClient.delete(any(DeleteRequest.class), eq(RequestOptions.DEFAULT))).thenThrow(new IOException());
        detectorMappingService.deleteDetectorMapping(id);
    }

    private SearchResponse mockSearchResponse(String searchIndex, int lookUpTime, String detectorUuid) {
        SearchResponse searchResponse = mock(SearchResponse.class);
        Map<String, DocumentField> fields = new HashMap<>();
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

    private GetResponse mockGetResponse(String id) {
        GetResponse getResponse = mock(GetResponse.class);
        Map<String, DocumentField> fields = new HashMap<>();
        val detectorUuid = "aeb4d849-847a-45c0-8312-dc0fcf22b639";
        String source = "{\"aa_user\":{\"id\":\"test-user\"},\"aa_detector\":" +
                "{\"id\":\"" + detectorUuid + "\"},\"aa_query\":{\"bool\":{\"must\":[{\"match\":" +
                "{\"name\":\"sample-web\",\"env\":\"prod\"}}]}},\"aa_enabled\":true,\"aa_lastModifiedTime\":1554828886," +
                "\"aa_createdTime\":1554828886}\n";
        when(getResponse.getSourceAsString()).thenReturn(source);
        when(getResponse.getId()).thenReturn(id);
        return getResponse;
    }

    private DeleteResponse mockDeleteResponse(String id) {
        DeleteResponse deleteResponse = mock(DeleteResponse.class);
        Result ResultOpt;
        when(deleteResponse.getId()).thenReturn(id);
        String indexName = elasticSearchProperties.getIndexName();
        when(deleteResponse.getIndex()).thenReturn(indexName);
        try {
            byte[] byteopt = new byte[]{2}; // 2 - DELETED, DeleteResponse.Result
            ByteBuffer byteBuffer = ByteBuffer.wrap(byteopt);
            ByteBufferStreamInput byteoptbytebufferstream = new ByteBufferStreamInput(byteBuffer);
            ResultOpt = DocWriteResponse.Result.readFrom(byteoptbytebufferstream);
            when(deleteResponse.getResult()).thenReturn(ResultOpt);
        } catch (IOException e) {
        }
        return deleteResponse;
    }
}
