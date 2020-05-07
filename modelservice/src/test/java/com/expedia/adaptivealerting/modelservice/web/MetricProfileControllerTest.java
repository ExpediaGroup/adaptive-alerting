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
package com.expedia.adaptivealerting.modelservice.web;

import com.expedia.adaptivealerting.modelservice.model.mapping.Expression;
import com.expedia.adaptivealerting.modelservice.repo.MetricProfileRepository;
import com.expedia.adaptivealerting.modelservice.web.request.CreateMetricProfilingRequest;
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
    private MetricProfileRepository metricProfileRepo;

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
        when(metricProfileRepo.createMetricProfile(Mockito.any(CreateMetricProfilingRequest.class))).thenReturn("created");
        when(metricProfileRepo.profilingExists(Mockito.any(Map.class))).thenReturn(true);
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
