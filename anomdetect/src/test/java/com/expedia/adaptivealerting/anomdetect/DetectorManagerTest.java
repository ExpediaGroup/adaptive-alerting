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
import com.expedia.adaptivealerting.anomdetect.util.ModelResource;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import lombok.val;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;

/**
 * {@link DetectorManager} unit test.
 *
 * @author Willie Wheeler
 */
public final class DetectorManagerTest {
    private static final String DETECTOR_TYPE = "ewma-detector";
    
    private DetectorManager manager;
    
    @Mock
    private DetectorSource detectorSource;
    
    private MappedMetricData mappedMetricData;
    private UUID detectorUuid;
    private ModelResource model;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
        this.manager = new DetectorManager(detectorSource);
    }
    
    @Test
    @Ignore
    public void testClassify() {
        val result = manager.classify(mappedMetricData);
        assertNotNull(result);
    }
    
    private void initTestObjects() {
        this.detectorUuid = UUID.randomUUID();
        
        val metricDef = new MetricDefinition("my-metric");
        val metricData = new MetricData(metricDef, 100.0, Instant.now().getEpochSecond());
        this.mappedMetricData = new MappedMetricData(metricData, detectorUuid, DETECTOR_TYPE);
        
        this.model = new ModelResource();
        model.setUuid(detectorUuid);
        model.setParams(new HashMap<>());
    }
    
    private void initDependencies() {
//        when(detectorSource.findDetector(any(DetectorMeta.class), any(MetricDefinition.class)))
//                .thenReturn(model);
    }
}
