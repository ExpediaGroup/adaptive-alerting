package com.expedia.adaptivealerting.modelservice.web;

import com.expedia.adaptivealerting.modelservice.dto.metricprofiling.CreateMetricProfilingRequest;
import com.expedia.adaptivealerting.modelservice.service.MetricProfilingService;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MetricProfilingControllerTest {

    @Spy
    @InjectMocks
    private MetricProfilingController controller;

    @Mock
    private MetricProfilingService profilingService;

    @Before
    public void setUp() {
        this.controller = new MetricProfilingController();
        MockitoAnnotations.initMocks(this);
        initDependencies();

    }

    private void initDependencies() {
        when(profilingService.createMetricProfile(Mockito.any(CreateMetricProfilingRequest.class))).thenReturn("created");
        when(profilingService.profilingExists(Mockito.any(Map.class))).thenReturn(true);
    }

    @Test
    public void testCreateMetricProfile() {
        val response = controller.createMetricProfile(new CreateMetricProfilingRequest());
        assertNotNull(response);
    }

    @Test
    public void testUpdateMetricProfile() {
        controller.updateMetricProfile("id", true);
        verify(controller, times(1)).updateMetricProfile("id", true);
    }

    @Test
    public void testSearchMetricProfiles() {
        val profileExists = controller.profilingExists(new HashMap<>());
        assertNotNull(profileExists);
    }

}
