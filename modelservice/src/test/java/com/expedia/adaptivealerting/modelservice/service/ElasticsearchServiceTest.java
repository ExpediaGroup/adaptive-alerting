package com.expedia.adaptivealerting.modelservice.service;

import com.expedia.adaptivealerting.modelservice.entity.ElasticsearchDetector;
import com.expedia.adaptivealerting.modelservice.repo.ElasticsearchDetectorRepository;
import com.expedia.adaptivealerting.modelservice.test.ObjectMother;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class ElasticsearchServiceTest {

    @InjectMocks
    private ElasticsearchService elasticsearchService = new ElasticsearchServiceImpl();

    @Mock
    private ElasticsearchDetectorRepository elasticSearchDetectorRepository;

    private ElasticsearchDetector elasticsearchDetector;

    private List<ElasticsearchDetector> elasticsearchDetectors = new ArrayList<>();

    @Before
    public void setUp() {
        initTestObjects();
        initDependencies();
    }

    @Test
    public void testToggleDetector() {
        elasticsearchService.toggleDetector("uuid", true);
    }

    @Test
    public void testGetLastUpdatedDetectors() {
        List<ElasticsearchDetector> actualDetectors = elasticsearchService.getLastUpdatedDetectors(10);
        assertNotNull(actualDetectors);
        Assert.assertEquals(1, actualDetectors.size());
        Assert.assertEquals("uuid", actualDetectors.get(0).getUuid());
        Assert.assertEquals("user", actualDetectors.get(0).getCreatedBy());
        Assert.assertEquals(true, actualDetectors.get(0).getEnabled());
    }


    private void initTestObjects() {
        ObjectMother mom = ObjectMother.instance();
        elasticsearchDetector = mom.getElasticsearchDetector();
        elasticsearchDetectors.add(elasticsearchDetector);
    }

    private void initDependencies() {
        Mockito.when(elasticSearchDetectorRepository.findElasticSearchDetectorByUuid(Mockito.anyString())).thenReturn(elasticsearchDetector);
        Mockito.when(elasticSearchDetectorRepository.getLastUpdatedDetectors(Mockito.anyString(), Mockito.anyString())).thenReturn(elasticsearchDetectors);
    }
}
