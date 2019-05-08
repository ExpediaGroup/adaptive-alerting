package com.expedia.adaptivealerting.modelservice.web;

import com.expedia.adaptivealerting.modelservice.entity.ElasticSearchDetector;
import com.expedia.adaptivealerting.modelservice.repo.ElasticSearchDetectorRepository;
import com.expedia.adaptivealerting.modelservice.service.DetectorService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertNotNull;


@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class DetectorControllerTest {

    @InjectMocks
    private DetectorController detectorController;

    @Mock
    private DetectorService detectorService;

    @Mock
    private ElasticSearchDetectorRepository repo;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testToggleDetector() {
        detectorService.toggleDetector("uuid", true);
    }

    @Test
    public void testGetLastUpdatedDetectors() {
        int interval = 5;
        List<ElasticSearchDetector> detectors = detectorService.getLastUpdatedDetectors(interval);
        assertNotNull(detectors);
    }

}
