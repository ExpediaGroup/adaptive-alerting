package com.expedia.adaptivealerting.modelservice.web;

import com.expedia.adaptivealerting.anomdetect.detect.outlier.OutlierDetectorResult;
import com.expedia.adaptivealerting.modelservice.repo.AnomalyRepository;
import com.expedia.adaptivealerting.modelservice.repo.request.AnomalyRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
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

    @Test(expected = IllegalArgumentException.class)
    public void testGetAnomalies_illegal_args() {
        when(anomalyRepository.getAnomalies(request)).thenReturn(null);
        controller.getAnomalies(request);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAnomalies_illegal_args1() {
        when(anomalyRepository.getAnomalies(request)).thenReturn(new ArrayList<>());
        controller.getAnomalies(request);
    }
}
