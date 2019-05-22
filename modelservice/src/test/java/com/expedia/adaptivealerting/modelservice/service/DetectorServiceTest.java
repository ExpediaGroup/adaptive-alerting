package com.expedia.adaptivealerting.modelservice.service;

import com.expedia.adaptivealerting.modelservice.elasticsearch.ElasticSearchClient;
import com.expedia.adaptivealerting.modelservice.entity.ElasticsearchDetector;
import com.expedia.adaptivealerting.modelservice.test.ObjectMother;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class DetectorServiceTest {

    @InjectMocks
    private DetectorService detectorService = new DetectorServiceImpl();

    @Mock
    private ElasticSearchClient elasticSearchClient;

    private ElasticsearchDetector elasticsearchDetector;

    private SearchResponse searchResponse;

    private List<ElasticsearchDetector> elasticsearchDetectors = new ArrayList<>();

    @Before
    public void setUp() {
        initTestObjects();
        initDependencies();
    }

    @Test
    public void testFindByUuid() {
        List<ElasticsearchDetector> actualDetectors = detectorService.findByUuid("uuid");
        assertNotNull(actualDetectors);
        Assert.assertEquals(1, actualDetectors.size());
        Assert.assertEquals("13456565", actualDetectors.get(0).getUuid());
        Assert.assertEquals("kashah", actualDetectors.get(0).getCreatedBy());
        Assert.assertEquals(true, actualDetectors.get(0).getEnabled());
    }

    @Test
    public void testFindByCreatedBy() {
        List<ElasticsearchDetector> actualDetectors = detectorService.findByCreatedBy("kashah");
        assertNotNull(actualDetectors);
        Assert.assertEquals(1, actualDetectors.size());
        Assert.assertEquals("13456565", actualDetectors.get(0).getUuid());
        Assert.assertEquals("kashah", actualDetectors.get(0).getCreatedBy());
        Assert.assertEquals(true, actualDetectors.get(0).getEnabled());
    }

    @Test
    public void testToggleDetector() {
        detectorService.toggleDetector("13456565", true);
    }

    @Test
    public void testGetLastUpdatedDetectors() {
        List<ElasticsearchDetector> actualDetectors = detectorService.getLastUpdatedDetectors(10);
        assertNotNull(actualDetectors);
        Assert.assertEquals(1, actualDetectors.size());
        Assert.assertEquals("13456565", actualDetectors.get(0).getUuid());
        Assert.assertEquals("kashah", actualDetectors.get(0).getCreatedBy());
        Assert.assertEquals(true, actualDetectors.get(0).getEnabled());
    }

    private void initTestObjects() {
        ObjectMother mom = ObjectMother.instance();
        elasticsearchDetector = mom.getElasticsearchDetector();
        elasticsearchDetectors.add(elasticsearchDetector);
        String searchIndex = "2";
        searchResponse = mockSearchResponse(searchIndex);
    }

    @SneakyThrows
    private void initDependencies() {
        Mockito.when(elasticSearchClient.search(any(SearchRequest.class), any(RequestOptions.class))).thenReturn(searchResponse);
    }

    private SearchResponse mockSearchResponse(String searchIndex) {
        SearchResponse searchResponse = mock(SearchResponse.class);
        Map<String, DocumentField> fields = new HashMap<>();
        SearchHit searchHit = new SearchHit(101, "xxx", null, fields);
        BytesReference source = new BytesArray("{\"uuid\":\"13456565\",\"createdBy\":\"kashah\",\"lastUpdateTimestamp\":\"2019-05-20 12:00:00\",\"enabled\":true,\"detectorConfig\":{\"hyperparams\":{\"alpha\":0.5,\"beta\":0.6},\"trainingMetaData\":{\"alpha\":0.5},\"params\":{\"upperWeak\":123}}}}");
        searchHit.sourceRef(source);
        SearchHit[] bunchOfSearchHits = new SearchHit[1];
        bunchOfSearchHits[0] = searchHit;
        SearchHits searchHits = new SearchHits(bunchOfSearchHits, 1, 1);
        when(searchResponse.getHits()).thenReturn(searchHits);
        return searchResponse;
    }
}