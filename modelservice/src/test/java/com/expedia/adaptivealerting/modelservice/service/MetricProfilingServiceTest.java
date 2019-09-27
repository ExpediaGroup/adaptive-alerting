package com.expedia.adaptivealerting.modelservice.service;

import com.expedia.adaptivealerting.modelservice.dto.metricprofiling.CreateMetricProfilingRequest;
import com.expedia.adaptivealerting.modelservice.dto.metricprofiling.MatchedMetricResponse;
import com.expedia.adaptivealerting.modelservice.repo.MetricProfilingRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class MetricProfilingServiceTest {

    @Spy
    @InjectMocks
    private MetricProfilingService profilingService = new MetricProfilingServiceImpl();

    @Mock
    private MetricProfilingRepository repository;

    private MatchedMetricResponse metricResponse;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
    }

    @Test
    public void testCreateMetricProfile() {
        String actualCreateId = profilingService.createMetricProfile(new CreateMetricProfilingRequest());
        assertNotNull(actualCreateId);
        assertEquals("1", actualCreateId);
    }

    @Test
    public void testUpdateMetricProfile() {
        profilingService.updateMetricProfile("id", true);
        verify(profilingService, times(1)).updateMetricProfile("id", true);
    }

    @Test
    public void testFindMatchingMetricProfiles() {
        MatchedMetricResponse response = profilingService.profilingExists(new HashMap<>());
        assertNotNull(response);
        assertEquals("1", response.getId());
    }

    private void initTestObjects() {
        this.metricResponse = new MatchedMetricResponse("1", 100L);
    }

    private void initDependencies() {
        Mockito.when(repository.createMetricProfile(Mockito.any(CreateMetricProfilingRequest.class))).thenReturn("1");
        Mockito.when(repository.profilingExists(Mockito.any(Map.class))).thenReturn(metricResponse);
    }

}
