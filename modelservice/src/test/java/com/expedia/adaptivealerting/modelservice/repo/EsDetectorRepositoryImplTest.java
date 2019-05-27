package com.expedia.adaptivealerting.modelservice.repo;

import com.expedia.adaptivealerting.modelservice.elasticsearch.ElasticSearchClient;
import com.expedia.adaptivealerting.modelservice.entity.ElasticsearchDetector;
import com.expedia.adaptivealerting.modelservice.repo.impl.EsDetectorRepositoryImpl;
import com.expedia.adaptivealerting.modelservice.test.ObjectMother;
import com.expedia.adaptivealerting.modelservice.util.ElasticsearchUtil;
import com.expedia.adaptivealerting.modelservice.util.ObjectMapperUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.io.stream.ByteBufferStreamInput;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class EsDetectorRepositoryImplTest {

    @InjectMocks
    private EsDetectorRepository detectorRepository = new EsDetectorRepositoryImpl();

    @Mock
    private ElasticSearchClient elasticSearchClient;

    @Mock
    private ElasticsearchUtil elasticsearchUtil;

    @Mock
    private ObjectMapperUtil objectMapperUtil;

    private ElasticsearchDetector elasticsearchDetector;

    private IndexResponse indexResponse;

    private SearchResponse searchResponse;

    private DeleteResponse deleteResponse;

    private GetResponse getResponse;

    private List<ElasticsearchDetector> elasticsearchDetectors = new ArrayList<>();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
    }

    @Test
    public void testCreateDetector() {
        String actualCreationId = detectorRepository.createDetector(new ElasticsearchDetector());
        assertNotNull(actualCreationId);
        assertEquals("1", actualCreationId);
    }

    @Test
    public void testDeleteDetector() {
        EsDetectorRepository detectorRepository = mock(EsDetectorRepository.class);
        doNothing().when(detectorRepository).deleteDetector(anyString());
        detectorRepository.deleteDetector("aeb4d849-847a-45c0-8312-dc0fcf22b639");
        verify(detectorRepository, times(1)).deleteDetector("aeb4d849-847a-45c0-8312-dc0fcf22b639");
    }

    @Test
    public void testUpdateDetector() {
        EsDetectorRepository detectorRepository = mock(EsDetectorRepository.class);
        doNothing().when(detectorRepository).updateDetector(anyString(), any(ElasticsearchDetector.class));
        detectorRepository.updateDetector("aeb4d849-847a-45c0-8312-dc0fcf22b639", new ElasticsearchDetector());
        verify(detectorRepository, times(1)).updateDetector("aeb4d849-847a-45c0-8312-dc0fcf22b639", new ElasticsearchDetector());
    }

    @Test
    public void testFindByUuid() {
        List<ElasticsearchDetector> actualDetectors = detectorRepository.findByUuid("uuid");
        assertNotNull(actualDetectors);
        assertCheck(actualDetectors);
    }

    @Test
    public void testFindByCreatedBy() {
        List<ElasticsearchDetector> actualDetectors = detectorRepository.findByCreatedBy("kashah");
        assertNotNull(actualDetectors);
        assertCheck(actualDetectors);
    }

    @Test
    public void testToggleDetector() {
        EsDetectorRepository detectorRepository = mock(EsDetectorRepository.class);
        doNothing().when(detectorRepository).toggleDetector(anyString(), anyBoolean());
        detectorRepository.toggleDetector("aeb4d849-847a-45c0-8312-dc0fcf22b639", true);
        verify(detectorRepository, times(1)).toggleDetector("aeb4d849-847a-45c0-8312-dc0fcf22b639", true);
    }

    @Test
    public void testGetLastUpdatedDetectors() {
        List<ElasticsearchDetector> actualDetectors = detectorRepository.getLastUpdatedDetectors("", "");
        assertNotNull(actualDetectors);
        assertCheck(actualDetectors);
    }

    @Test(expected = RuntimeException.class)
    public void searchDetectorFail() throws IOException {
        Mockito.when(elasticSearchClient.search(any(SearchRequest.class), any(RequestOptions.class))).thenThrow(new IOException());
        detectorRepository.findByUuid("aeb4d849-847a-45c0-8312-dc0fcf22b639");
    }

    @Test(expected = RuntimeException.class)
    public void updateDetectorFail() throws IOException {
        Mockito.when(elasticSearchClient.update(any(UpdateRequest.class), any(RequestOptions.class))).thenThrow(new IOException());
        detectorRepository.toggleDetector("aeb4d849-847a-45c0-8312-dc0fcf22b639", true);
    }

    @Test(expected = RuntimeException.class)
    public void deleteDetectorFail() throws IOException {
        Mockito.when(elasticSearchClient.delete(any(DeleteRequest.class), any(RequestOptions.class))).thenThrow(new IOException());
        detectorRepository.deleteDetector("aeb4d849-847a-45c0-8312-dc0fcf22b639");
    }

    private void initTestObjects() {
        ObjectMother mom = ObjectMother.instance();
        elasticsearchDetector = mom.getElasticsearchDetector();
        elasticsearchDetectors.add(elasticsearchDetector);
        String searchIndex = "2";
        indexResponse = mockIndexResponse();
        searchResponse = mockSearchResponse(searchIndex);
        deleteResponse = mockDeleteResponse("id");
        getResponse = mockGetResponse("id");
    }

    @SneakyThrows
    private void initDependencies() {
        Mockito.when(elasticsearchUtil.getSourceBuilder(any(QueryBuilder.class))).thenReturn(new SearchSourceBuilder());
        Mockito.when(elasticsearchUtil.getSearchRequest(any(SearchSourceBuilder.class), anyString(), anyString())).thenReturn(new SearchRequest());
        Mockito.when(elasticsearchUtil.getIndexResponse(any(IndexRequest.class), anyString())).thenReturn(indexResponse);

        Mockito.when(objectMapperUtil.convertToString(any())).thenReturn(new String());
        Mockito.when(objectMapperUtil.convertToObject(anyString(), any())).thenReturn(elasticsearchDetector);
        Mockito.when(elasticSearchClient.search(any(SearchRequest.class), any(RequestOptions.class))).thenReturn(searchResponse);
        Mockito.when(elasticSearchClient.get(any(GetRequest.class), any(RequestOptions.class))).thenReturn(getResponse);
        Mockito.when(elasticSearchClient.delete(any(DeleteRequest.class), any(RequestOptions.class))).thenReturn(deleteResponse);
        Mockito.when(elasticSearchClient.update(any(UpdateRequest.class), any(RequestOptions.class))).thenReturn(new UpdateResponse());

    }

    private SearchResponse mockSearchResponse(String searchIndex) {
        SearchResponse searchResponse = mock(SearchResponse.class);
        Map<String, DocumentField> fields = new HashMap<>();
        SearchHit searchHit = new SearchHit(101, "xxx", null, fields);
        BytesReference source = new BytesArray("{\"uuid\":\"13456565\",\"createdBy\":\"user\",\"lastUpdateTimestamp\":\"2019-05-20 12:00:00\",\"enabled\":true,\"detectorConfig\":{\"hyperparams\":{\"alpha\":0.5,\"beta\":0.6},\"trainingMetaData\":{\"alpha\":0.5},\"params\":{\"upperWeak\":123}}}}");
        searchHit.sourceRef(source);
        SearchHit[] bunchOfSearchHits = new SearchHit[1];
        bunchOfSearchHits[0] = searchHit;
        SearchHits searchHits = new SearchHits(bunchOfSearchHits, 1, 1);
        when(searchResponse.getHits()).thenReturn(searchHits);
        return searchResponse;
    }

    private IndexResponse mockIndexResponse() {
        IndexResponse indexResponse = mock(IndexResponse.class);
        when(indexResponse.getId()).thenReturn("1");
        return indexResponse;
    }

    private DeleteResponse mockDeleteResponse(String id) {
        DeleteResponse deleteResponse = mock(DeleteResponse.class);
        Result ResultOpt;
        when(deleteResponse.getId()).thenReturn(id);
        String indexName = "index";
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

    private GetResponse mockGetResponse(String id) {
        GetResponse getResponse = mock(GetResponse.class);
        Map<String, DocumentField> fields = new HashMap<>();
        String source = "{\"uuid\":\"aeb4d849-847a-45c0-8312-dc0fcf22b639\",\"createdBy\":\"test-user\",\"lastUpdateTimestamp\":\"2019-05-20 12:00:00\",\"enabled\":true,\"detectorConfig\":{\"hyperparams\":{\"alpha\":0.5,\"beta\":0.6},\"trainingMetaData\":{\"alpha\":0.5},\"params\":{\"upperWeak\":123}}}}";
        when(getResponse.getSourceAsString()).thenReturn(source);
        when(getResponse.getId()).thenReturn(id);
        return getResponse;
    }

    private void assertCheck(List<ElasticsearchDetector> actualDetectors) {
        Assert.assertEquals(1, actualDetectors.size());
        Assert.assertEquals("aeb4d849-847a-45c0-8312-dc0fcf22b639", actualDetectors.get(0).getUuid());
        Assert.assertEquals("test-user", actualDetectors.get(0).getCreatedBy());
        Assert.assertEquals(true, actualDetectors.get(0).getEnabled());
    }
}