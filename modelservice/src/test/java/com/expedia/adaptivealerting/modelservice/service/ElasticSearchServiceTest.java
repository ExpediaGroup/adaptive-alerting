package com.expedia.adaptivealerting.modelservice.service;

import com.expedia.adaptivealerting.modelservice.entity.ElasticSearchDetector;
import com.expedia.adaptivealerting.modelservice.repo.ElasticSearchDetectorRepository;
import com.expedia.adaptivealerting.modelservice.test.ObjectMother;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class ElasticSearchServiceTest {

    @InjectMocks
    private ElasticSearchService elasticSearchService = new ElasticSearchServiceImpl();

    @Mock
    private ElasticSearchDetectorRepository elasticSearchDetectorRepository;

    private ElasticSearchDetector elasticSearchDetector;

    private List<ElasticSearchDetector> elasticSearchDetectors = new ArrayList<>();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
    }

    @Test
    public void testToggleDetector() {
        elasticSearchService.toggleDetector("uuid", true);
    }

    @Test
    public void testGetLastUpdatedDetectors() {
        List<ElasticSearchDetector> actualDetectors = elasticSearchService.getLastUpdatedDetectors(10);
        assertNotNull(actualDetectors);
        Assert.assertEquals(1, actualDetectors.size());
        Assert.assertEquals("uuid", actualDetectors.get(0).getUuid());
        Assert.assertEquals("user", actualDetectors.get(0).getCreatedBy());
        Assert.assertEquals(true, actualDetectors.get(0).getEnabled());
    }


    private void initTestObjects() {
        ObjectMother mom = ObjectMother.instance();
        elasticSearchDetector = mom.getElasticSearchDetector();
        elasticSearchDetectors.add(elasticSearchDetector);
    }

    private void initDependencies() {
        Mockito.when(elasticSearchDetectorRepository.findElasticSearchDetectorByUuid(Mockito.anyString())).thenReturn(elasticSearchDetector);
        Mockito.when(elasticSearchDetectorRepository.getLastUpdatedDetectors(Mockito.anyString(), Mockito.anyString())).thenReturn(elasticSearchDetectors);
    }
}
