package com.expedia.adaptivealerting.modelservice.web;

import com.expedia.adaptivealerting.modelservice.entity.ElasticSearchDetector;
import com.expedia.adaptivealerting.modelservice.repo.ElasticSearchDetectorRepository;
import com.expedia.adaptivealerting.modelservice.service.ElasticSearchService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;


@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class DetectorControllerTest {

    @Mock
    private ElasticSearchService elasticSearchService;

    @Mock
    private ElasticSearchDetectorRepository repo;

    @Before
    public void setUp() {
    }

    @Test
    public void testToggleDetector() {
        elasticSearchService.toggleDetector("uuid", true);
        verify(elasticSearchService, atLeastOnce()).toggleDetector("uuid", true);
    }

    @Test
    public void testGetLastUpdatedDetectors() {
        int interval = 5;
        List<ElasticSearchDetector> detectors = elasticSearchService.getLastUpdatedDetectors(interval);
        assertNotNull(detectors);
    }

}
