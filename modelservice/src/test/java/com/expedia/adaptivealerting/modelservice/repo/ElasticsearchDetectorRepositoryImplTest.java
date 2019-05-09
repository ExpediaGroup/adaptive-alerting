package com.expedia.adaptivealerting.modelservice.repo;

import com.expedia.adaptivealerting.modelservice.entity.ElasticsearchDetector;
import com.expedia.adaptivealerting.modelservice.repo.impl.ElasticsearchDetectorRepositoryImpl;
import com.expedia.adaptivealerting.modelservice.test.ObjectMother;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class ElasticsearchDetectorRepositoryImplTest {

    @InjectMocks
    private ElasticsearchDetectorRepositoryImpl elasticsearchServiceImpl;

    @Mock
    private ElasticsearchTemplate elasticsearchTemplate;

    @Mock
    private ListenableActionFuture listenableActionFuture;

    @Mock
    private SearchResponse searchResponse;

    @Mock
    private SearchHits hits;

    @Mock
    private SearchHit hit;

    @Mock
    private Client elasticSearchClient;

    @Mock(answer = Answers.RETURNS_SELF)
    private SearchRequestBuilder searchRequestBuilder;

    private ElasticsearchDetector elasticsearchDetector;

    private List<ElasticsearchDetector> elasticsearchDetectors = new ArrayList<>();

    private Map<String, Object> source;

    @Before
    public void setUp() {
        initTestObjects();
        initDependencies();
    }

    @Test
    public void testToggleDetector() {
        elasticsearchServiceImpl.toggleDetector(elasticsearchDetector, true);
    }

    @Test
    public void testGetLastUpdatedDetectors() {
        List<ElasticsearchDetector> actualDetectors = elasticsearchServiceImpl.getLastUpdatedDetectors("", "");
        assertNotNull(actualDetectors);
        Assert.assertEquals(1, actualDetectors.size());
        ElasticsearchDetector actualDetector = actualDetectors.get(0);
        Assert.assertEquals("kashah", actualDetector.getCreatedBy());
        Assert.assertEquals("uuid", actualDetector.getUuid());
        Assert.assertEquals(true, actualDetector.getEnabled());
    }

    private void initTestObjects() {
        ObjectMother mom = ObjectMother.instance();
        elasticsearchDetector = mom.getElasticsearchDetector();
        elasticsearchDetectors.add(elasticsearchDetector);
        source = mom.getElasticSearchSource();
    }

    private void initDependencies() {
        Mockito.when(elasticSearchClient.prepareSearch()).thenReturn(searchRequestBuilder);
        Mockito.when(searchRequestBuilder.execute()).thenReturn(listenableActionFuture);
        Mockito.when(listenableActionFuture.actionGet()).thenReturn(searchResponse);
        Mockito.when(searchResponse.getHits()).thenReturn(hits);
        Mockito.when(hits.getHits()).thenReturn(new SearchHit[]{hit});
        Mockito.when(hit.getSource()).thenReturn(source);
    }
}
