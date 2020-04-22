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

import com.expedia.adaptivealerting.anomdetect.detect.Detector;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorContainer;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorRequest;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorResponse;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorResult;
import com.expedia.adaptivealerting.anomdetect.filter.DetectionFilter;
import com.expedia.adaptivealerting.anomdetect.filter.chain.DetectionFilterChain;
import com.expedia.metrics.MetricData;
import com.google.common.collect.ImmutableList;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DetectorExecutorImplTest {

    private DetectorExecutorImpl executorUnderTest = new DetectorExecutorImpl();
    @Mock
    private Detector detector;
    @Mock
    private MetricData metricData;
    @Mock
    private DetectionFilter detectionFilter;
    @Mock
    private DetectorResult detectorResult;
    @Mock
    private DetectorContainer detectorContainer;
    @Mock
    private DetectorRequest detectorRequest;
    @Mock
    private DetectorResponse detectorResponse;

    @Before
    public void setUp() {
        initDependencies();
    }

    @Test
    public void doDetectionWithFiltering() {
        when(detectorContainer.getFilters()).thenReturn(ImmutableList.of(detectionFilter));
        val filterChain = new DetectionFilterChain(detectorContainer);

        doAnswer(invocation -> {
            DetectorResponse response = invocation.getArgument(1);
            response.setDetectorResult(detectorResult);
            return null;
        }).when(detectionFilter).doFilter(any(DetectorRequest.class), any(DetectorResponse.class), any(DetectionFilterChain.class));

        DetectorResult actualResult = executorUnderTest.doDetection(detectorContainer, metricData);
        assertSame(detectorResult, actualResult);
    }

    @Test
    public void doDetectionWithNoFiltering() {
        DetectorResult actualResult = executorUnderTest.doDetection(detectorContainer, metricData);
        assertSame(detectorResult, actualResult);
    }

    @Test(expected = NullPointerException.class)
    public void nullMetricDataNotAllowed() {
        executorUnderTest.doDetection(detectorContainer, null);
    }

    private void initDependencies() {
        when(detectorContainer.getDetector()).thenReturn(detector);
        when(detector.detect(metricData)).thenReturn(detectorResult);
    }
}
