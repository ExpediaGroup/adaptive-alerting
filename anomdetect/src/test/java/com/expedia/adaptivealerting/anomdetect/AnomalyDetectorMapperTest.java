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

import com.expedia.adaptivealerting.anomdetect.util.ModelServiceConnector;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.TagCollection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

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
    
    @Mock
    private ModelServiceConnector modelServiceConnector;
    
    // Test objects
    private MetricDefinition mappedDefinition;
    private MetricDefinition unmappedDefinition;
    private MetricData mappedData;
    private MetricData unmappedData;
    private AnomalyDetectorMeta detectorMeta;

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
        this.mappedDefinition = new MetricDefinition(new TagCollection(
                new HashMap<String, String>() {{
                    put("unit", "dummy");
                    put("mtype", "dummy");
                    put("what", "bookings");
                }}));
        this.unmappedDefinition = new MetricDefinition(new TagCollection(
                new HashMap<String, String>() {{
                    put("unit", "dummy");
                    put("mtype", "dummy");
                }}));
        
        this.mappedData = new MetricData(mappedDefinition, 9, System.currentTimeMillis());
        this.unmappedData = new MetricData(unmappedDefinition, 9, System.currentTimeMillis());
        
        this.detectorMeta = new AnomalyDetectorMeta(UUID.randomUUID(), "my-detector");
    }
    
    private void initDependencies() {
        when(modelServiceConnector.findDetectors(mappedDefinition))
                .thenReturn(Collections.singleton(detectorMeta));
        when(modelServiceConnector.findDetectors(unmappedDefinition))
                .thenReturn(Collections.emptySet());
    }
}
