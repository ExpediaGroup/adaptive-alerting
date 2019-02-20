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
package com.expedia.adaptivealerting.anomdetect;

import com.expedia.adaptivealerting.anomdetect.source.DetectorSource;
import com.expedia.adaptivealerting.anomdetect.util.DetectorMeta;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * {@link DetectorMapper} unit test.
 */
public final class DetectorMapperTest {
    private DetectorMapper mapper;
    
    @Mock
    private DetectorSource detectorSource;
    
    @Mock
    private MetricDefinition mappedDefinition;
    
    @Mock
    private MetricDefinition unmappedDefinition;
    
    private MetricData mappedData;
    private MetricData unmappedData;
    private DetectorMeta detectorMeta;
    private List<DetectorMeta> detectorMetas;
    private List<DetectorMeta> emptyDetectorMetas;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
        this.mapper = new DetectorMapper(detectorSource);
    }
    
    @Test
    public void testConstructorInjection() {
        assertSame(detectorSource, mapper.getDetectorSource());
    }
    
    @Test(expected = NullPointerException.class)
    public void testModelServiceConnectorNotNull() {
        new DetectorMapper(null);
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
        
        this.detectorMeta = new DetectorMeta(
                UUID.fromString("7629c28a-5958-4ca7-9aaa-49b95d3481ff"),
                "ewma-detector");
        this.detectorMetas = Collections.singletonList(detectorMeta);
        this.emptyDetectorMetas = Collections.EMPTY_LIST;
    }
    
    private void initDependencies() {
        when(detectorSource.findDetectorMetas(mappedDefinition)).thenReturn(detectorMetas);
        when(detectorSource.findDetectorMetas(unmappedDefinition)).thenReturn(emptyDetectorMetas);
    }
}
