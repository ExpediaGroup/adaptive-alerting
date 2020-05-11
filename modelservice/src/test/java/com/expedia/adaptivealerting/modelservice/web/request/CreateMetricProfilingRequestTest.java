/*
 * Copyright 2018-2019 Expedia Group, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expedia.adaptivealerting.modelservice.web.request;

import com.expedia.adaptivealerting.modelservice.domain.mapping.Expression;
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
