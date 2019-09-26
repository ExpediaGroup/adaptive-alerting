package com.expedia.adaptivealerting.modelservice.web;

import com.expedia.adaptivealerting.modelservice.dto.common.Expression;
import com.expedia.adaptivealerting.modelservice.dto.metricprofiling.CreateMetricProfilingRequest;
import com.expedia.adaptivealerting.modelservice.service.MetricProfilingService;
import com.expedia.adaptivealerting.modelservice.test.ObjectMother;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MetricProfileControllerTest {

    @Spy
    @InjectMocks
    private MetricProfileController controllerUnderTest;

    @Mock
    private MetricProfilingService profilingService;

    private Expression expression;

    @Before
    public void setUp() {
        this.controllerUnderTest = new MetricProfileController();
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
    }

    private void initTestObjects() {
        ObjectMother mom = ObjectMother.instance();
        this.expression = mom.getExpression();
    }

    private void initDependencies() {
        when(profilingService.createMetricProfile(Mockito.any(CreateMetricProfilingRequest.class))).thenReturn("created");
        when(profilingService.profilingExists(Mockito.any(Map.class))).thenReturn(true);
    }

    @Test
    public void testCreateMetricProfile() {
        val createMetricProfilingRequest = new CreateMetricProfilingRequest();
        createMetricProfilingRequest.setExpression(expression);
        createMetricProfilingRequest.setIsStationary(true);
        val response = controllerUnderTest.createMetricProfile(createMetricProfilingRequest);
        assertNotNull(response);
    }

    @Test
    public void testUpdateMetricProfile() {
        controllerUnderTest.updateMetricProfile("id", true);
        verify(controllerUnderTest, times(1)).updateMetricProfile("id", true);
    }

    @Test
    public void testSearchMetricProfiles() {
        val profileExists = controllerUnderTest.profilingExists(new HashMap<>());
        assertNotNull(profileExists);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateMetricProfile_notNull() throws IOException {
        controllerUnderTest.createMetricProfile(new CreateMetricProfilingRequest());
    }

}
