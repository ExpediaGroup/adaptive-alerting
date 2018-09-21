/*
 * Copyright 2018 Expedia Group, Inc.
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
package com.expedia.adaptivealerting.anomdetect.util;

import com.expedia.adaptivealerting.anomdetect.AnomalyDetectorMeta;
import com.expedia.metrics.MetricDefinition;
import org.apache.http.client.HttpClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Set;

import static org.junit.Assert.assertNotNull;

/**
 * {@link ModelServiceConnector} unit tests.R
 *
 * @author Willie Wheeler
 */
public class ModelServiceConnectorTest {
    
    // Class under test
    private ModelServiceConnector connector;
    
    @Mock
    private HttpClient httpClient;
    
    // Test objects
    private MetricDefinition metricDefinition;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
        this.connector = new ModelServiceConnector(httpClient);
    }
    
    @Test
    public void testConstructorInjection() {
        assertNotNull(connector.getHttpClient());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testHttpClientNotNull() {
        new ModelServiceConnector(null);
    }
    
    @Test
    public void testFindDetectors() {
        final Set<AnomalyDetectorMeta> results = connector.findDetectors(metricDefinition);
    }
    
    private void initTestObjects() {
    }
    
    private void initDependencies() {
    }
}
