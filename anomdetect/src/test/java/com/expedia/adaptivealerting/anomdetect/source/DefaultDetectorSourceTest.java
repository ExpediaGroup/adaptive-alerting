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
package com.expedia.adaptivealerting.anomdetect.source;

import com.expedia.adaptivealerting.anomdetect.util.DetectorResource;
import com.expedia.adaptivealerting.anomdetect.util.ModelServiceConnector;
import com.expedia.adaptivealerting.anomdetect.util.ModelTypeResource;
import com.expedia.metrics.MetricDefinition;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.hateoas.Resources;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@Slf4j
public class DefaultDetectorSourceTest {
    private static final String DETECTOR_UUID = "90c37a3c-f6bb-4c00-b41b-191909cccfb7";
    private static final String DETECTOR_TYPE = "ewma-detector";
    
    private DefaultDetectorSource source;
    
    @Mock
    private ModelServiceConnector connector;
    
    private MetricDefinition metricDef;
    private Resources<DetectorResource> detectorResources;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.source = new DefaultDetectorSource(connector);
        initTestObjects();
        initDependencies();
    }
    
    @Test
    public void testFindDetectorMetas() {
        val results = source.findDetectorMetas(metricDef);
        assertEquals(1, results.size());
        
        val result = results.get(0);
        assertEquals(DETECTOR_UUID, result.getUuid().toString());
        assertEquals(DETECTOR_TYPE, result.getType());
    }
    
    private void initTestObjects() {
        this.metricDef = new MetricDefinition("my-metric");
        
        val detectorResource = new DetectorResource(DETECTOR_UUID, new ModelTypeResource(DETECTOR_TYPE));
        this.detectorResources = new Resources<>(Collections.singletonList(detectorResource));
    }
    
    private void initDependencies() {
        when(connector.findDetectors(metricDef)).thenReturn(detectorResources);
    }
}
