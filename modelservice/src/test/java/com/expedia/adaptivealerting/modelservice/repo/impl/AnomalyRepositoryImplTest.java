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
package com.expedia.adaptivealerting.modelservice.repo.impl;

import com.expedia.adaptivealerting.anomdetect.detect.Detector;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.OutlierDetectorResult;
import com.expedia.adaptivealerting.anomdetect.source.DetectorDocument;
import com.expedia.adaptivealerting.anomdetect.source.DetectorFactory;
import com.expedia.adaptivealerting.modelservice.metricsource.graphite.GraphiteMetricSource;
import com.expedia.adaptivealerting.modelservice.metricsource.MetricSource;
import com.expedia.adaptivealerting.modelservice.metricsource.MetricSourceResult;
import com.expedia.adaptivealerting.modelservice.repo.impl.AnomalyRepositoryImpl;
import com.expedia.adaptivealerting.modelservice.repo.request.AnomalyRequest;
import com.expedia.adaptivealerting.modelservice.test.ObjectMother;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class AnomalyRepositoryImplTest {

    @InjectMocks
    private AnomalyRepositoryImpl serviceUnderTest;

    @Spy
    @Qualifier("metricSourceServiceListFactoryBean")
    private List<MetricSource> metricSources = new ArrayList<>();

    @Mock
    private GraphiteMetricSource graphiteMetricSource;

    @Spy
    private MetricSourceResult metricSourceResult;

    @Spy
    private List<MetricSourceResult> metricSourceResults = new ArrayList<>();

    @Mock
    private DetectorFactory detectorFactory;

    @Mock
    private Detector detector;

    private AnomalyRequest anomalyRequest;

    @Mock
    private OutlierDetectorResult outlierDetectorResult;

    @Before
    public void setUp() {
        this.serviceUnderTest = new AnomalyRepositoryImpl();
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
    }

    @Test
    public void testGetAnomalies() {
        val actualResults = serviceUnderTest.getAnomalies(anomalyRequest);
        assertNotNull(actualResults);
        assertEquals(1, actualResults.size());
//        assertEquals(AnomalyLevel.WEAK, actualResults.get(0).getAnomalyLevel());
        verify(graphiteMetricSource, atMost(1)).getMetricData(anyString());
    }

    private void initTestObjects() {
        val mom = ObjectMother.instance();
        this.anomalyRequest = mom.getAnomalyRequest();
        this.metricSourceResult = mom.getMetricData();
        this.metricSourceResults.add(metricSourceResult);
    }

    private void initDependencies() {
        when(graphiteMetricSource.getMetricData(anyString())).thenReturn(metricSourceResults);
        metricSources.add(graphiteMetricSource);
        when(detectorFactory.buildDetector(any(DetectorDocument.class))).thenReturn(detector);
    }
}
