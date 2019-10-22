package com.expedia.adaptivealerting.modelservice.web;

import com.expedia.adaptivealerting.anomdetect.detect.outlier.OutlierDetectorResult;
import com.expedia.adaptivealerting.modelservice.repo.request.AnomalyRequest;
import com.expedia.adaptivealerting.modelservice.repo.AnomalyRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

public class AnomalyControllerTest {

    // Class under test
    @InjectMocks
    private AnomalyController controller;

    // Dependencies
    @Mock
    private AnomalyRepository anomalyRepository;

    // Test objects
    @Mock
    private AnomalyRequest request;
    @Mock
    private List<OutlierDetectorResult> results;

    @Before
    public void setUp() {
        this.controller = new AnomalyController();
        MockitoAnnotations.initMocks(this);
        when(anomalyRepository.getAnomalies(request)).thenReturn(results);
    }

    @Test
    public void testGetAnomalies() {
        List<OutlierDetectorResult> actualResults = controller.getAnomalies(request);
        assertNotNull(actualResults);
        assertSame(results, actualResults);
    }
}
