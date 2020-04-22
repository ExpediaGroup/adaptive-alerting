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
package com.expedia.adaptivealerting.anomdetect.filter.chain;

import com.expedia.adaptivealerting.anomdetect.detect.Detector;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorContainer;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorRequest;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorResponse;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorResult;
import com.expedia.adaptivealerting.anomdetect.filter.DetectionFilter;
import com.expedia.metrics.MetricData;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DetectionFilterChainTest {
    private DetectionFilterChain chainUnderTest;

    @Mock
    private DetectorRequest detectorRequest;
    @Mock
    private DetectorResponse detectorResponse;
    @Mock
    private DetectorContainer detectorContainer;
    @Mock
    private DetectorResult detectorResult;
    @Mock
    private Detector detector;
    @Mock
    private MetricData metricData;

    @Test
    public void doFilterWithEmptyChain() {
        when(detectorContainer.getDetector()).thenReturn(detector);
        when(detectorRequest.getMetricData()).thenReturn(metricData);
        chainUnderTest = new DetectionFilterChain(detectorContainer);
        chainUnderTest.doFilter(detectorRequest, detectorResponse);
    }

    @Test
    public void doFilterWithChain_ensureChainKeepsResultFromFilter() {
        DetectionFilter stubDetectionFilter = (detectorRequest, detectorResponse, chain) -> detectorResponse.setDetectorResult(detectorResult);
        DetectorContainer detectorContainer = new DetectorContainer(detector, ImmutableList.of(stubDetectionFilter));
        chainUnderTest = new DetectionFilterChain(detectorContainer);

        DetectorResponse actualDetectorResponse = new DetectorResponse();
        chainUnderTest.doFilter(detectorRequest, actualDetectorResponse);

        assertSame(detectorResult, actualDetectorResponse.getDetectorResult());
    }

    @Test(expected = NullPointerException.class)
    public void nullDetectorRequestNotAllowed() {
        chainUnderTest.doFilter(null, detectorResponse);
    }

    @Test(expected = NullPointerException.class)
    public void nullDetectorResponseNotAllowed() {
        chainUnderTest.doFilter(detectorRequest, null);
    }

}
