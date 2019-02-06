package com.expedia.adaptivealerting.modelservice.web;

import com.expedia.adaptivealerting.modelservice.service.AnomalyRequest;
import com.expedia.adaptivealerting.modelservice.service.AnomalyService;
import com.expedia.adaptivealerting.modelservice.service.ModifiedAnomalyResult;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import java.util.List;

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
    private List<ModifiedAnomalyResult> results;

    @Before
    public void setUp() {
        this.controller = new AnomalyController();
        MockitoAnnotations.initMocks(this);
        when(anomalyService.getAnomalies(request)).thenReturn(results);
    }

    @Test
    public void testGetAnomalies() {
        List<ModifiedAnomalyResult> actualResults = controller.getAnomalies(request);
        assertNotNull(actualResults);
        assertSame(results, actualResults);
    }
}
