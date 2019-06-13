package com.expedia.adaptivealerting.modelservice.web;

import com.expedia.adaptivealerting.anomdetect.outlier.AnomalyResult;
import com.expedia.adaptivealerting.modelservice.service.AnomalyRequest;
import com.expedia.adaptivealerting.modelservice.service.AnomalyService;
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
    private AnomalyService anomalyService;

    // Test objects
    @Mock
    private AnomalyRequest request;
    @Mock
    private List<AnomalyResult> results;

    @Before
    public void setUp() {
        this.controller = new AnomalyController();
        MockitoAnnotations.initMocks(this);
        when(anomalyService.getAnomalies(request)).thenReturn(results);
    }

    @Test
    public void testGetAnomalies() {
        List<AnomalyResult> actualResults = controller.getAnomalies(request);
        assertNotNull(actualResults);
        assertSame(results, actualResults);
    }
}
