package com.expedia.adaptivealerting.modelservice.web;

import com.expedia.adaptivealerting.modelservice.entity.ElasticsearchDetector;
import com.expedia.adaptivealerting.modelservice.repo.ElasticsearchDetectorRepository;
import com.expedia.adaptivealerting.modelservice.service.ElasticsearchService;
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
    private ElasticsearchService elasticsearchService;

    @Mock
    private ElasticsearchDetectorRepository repo;

    @Before
    public void setUp() {
    }

    @Test
    public void testToggleDetector() {
        elasticsearchService.toggleDetector("uuid", true);
        verify(elasticsearchService, atLeastOnce()).toggleDetector("uuid", true);
    }

    @Test
    public void testGetLastUpdatedDetectors() {
        int interval = 5;
        List<ElasticsearchDetector> detectors = elasticsearchService.getLastUpdatedDetectors(interval);
        assertNotNull(detectors);
    }

}
