package com.expedia.adaptivealerting.modelservice.dto;

import com.expedia.adaptivealerting.modelservice.dto.common.Expression;
import com.expedia.adaptivealerting.modelservice.dto.metricprofiling.CreateMetricProfilingRequest;
import com.expedia.adaptivealerting.modelservice.test.ObjectMother;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertNotEquals;

public class CreateMetricProfilingRequestTest {

    private Expression expression;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
    }

    private void initTestObjects() {
        ObjectMother mom = ObjectMother.instance();
        this.expression = mom.getExpression();
    }

    @Test
    public void testEquals() {
        CreateMetricProfilingRequest createMetricProfilingRequest = new CreateMetricProfilingRequest();
        createMetricProfilingRequest.setExpression(expression);
        createMetricProfilingRequest.setIsStationary(true);
        assertNotEquals(createMetricProfilingRequest, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNull_isStationary_request() {
        CreateMetricProfilingRequest createMetricProfilingRequest = new CreateMetricProfilingRequest();
        createMetricProfilingRequest.setExpression(expression);
        createMetricProfilingRequest.validate();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNull_expression_request() {
        CreateMetricProfilingRequest createMetricProfilingRequest = new CreateMetricProfilingRequest();
        createMetricProfilingRequest.setIsStationary(true);
        createMetricProfilingRequest.validate();
    }
}
