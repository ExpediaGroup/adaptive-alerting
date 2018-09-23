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
package com.expedia.adaptivealerting.anomdetect;

import com.expedia.adaptivealerting.anomdetect.util.ModelResource;
import com.expedia.adaptivealerting.anomdetect.util.ModelServiceConnector;
import com.expedia.adaptivealerting.anomdetect.util.ModelTypeResource;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.hateoas.Resources;

import java.util.Collections;
import java.util.Set;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

/**
 * {@link AnomalyDetectorMapper} unit tests.
 *
 * @author Willie Wheeler
 */
public final class AnomalyDetectorMapperTest {
    
    // Class under test
    private AnomalyDetectorMapper mapper;
    
    // Dependencies
    
    @Mock
    private ModelServiceConnector modelServiceConnector;
    
    // Test objects
    
    @Mock
    private MetricDefinition mappedDefinition;
    
    @Mock
    private MetricDefinition unmappedDefinition;
    
    private MetricData mappedData;
    private MetricData unmappedData;
    private ModelResource modelResource;
    private Resources<ModelResource> modelResources;
    private Resources<ModelResource> emptyModelResources;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
        this.mapper = new AnomalyDetectorMapper(modelServiceConnector);
    }
    
    @Test
    public void testConstructorInjection() {
        assertSame(modelServiceConnector, mapper.getModelServiceConnector());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testModelServiceConnectorNotNull() {
        new AnomalyDetectorMapper(null);
    }
    
    @Test
    public void testMap_metricDataWithDetectors() {
        final Set<MappedMetricData> results = mapper.map(mappedData);
        assertFalse(results.isEmpty());
    }
    
    @Test
    public void testMap_metricDataWithoutDetectors() {
        final Set<MappedMetricData> results = mapper.map(unmappedData);
        assertTrue(results.isEmpty());
    }
    
    private void initTestObjects() {
        this.mappedData = new MetricData(mappedDefinition, 9, System.currentTimeMillis());
        this.unmappedData = new MetricData(unmappedDefinition, 9, System.currentTimeMillis());
        
        this.modelResource = new ModelResource(
                "7629c28a-5958-4ca7-9aaa-49b95d3481ff",
                new ModelTypeResource("ewma-detector"));
        
        this.modelResources = new Resources<>(Collections.singletonList(modelResource));
        this.emptyModelResources = new Resources<>(Collections.EMPTY_LIST);
    }
    
    private void initDependencies() {
        when(modelServiceConnector.findModels(mappedDefinition))
                .thenReturn(modelResources);
        when(modelServiceConnector.findModels(unmappedDefinition))
                .thenReturn(emptyModelResources);
    }
}
