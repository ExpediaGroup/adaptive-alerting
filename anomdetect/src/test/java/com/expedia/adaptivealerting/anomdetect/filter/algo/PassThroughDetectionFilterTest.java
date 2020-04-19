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
package com.expedia.adaptivealerting.anomdetect.filter.algo;

import com.expedia.adaptivealerting.anomdetect.detect.DetectorRequest;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorResponse;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.OutlierDetectorResult;
import com.expedia.adaptivealerting.anomdetect.filter.chain.DetectionFilterChain;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class PassThroughDetectionFilterTest {
    private PassThroughDetectionFilter filterUnderTest;
    private OutlierDetectorResult outlierDetectorResult;
    private MetricData metricData;
    @Mock
    private DetectionFilterChain mockChain;
    @Mock
    private DetectorRequest detectorRequest;
    @Mock
    private DetectorResponse detectorResponse;

    @Before
    public void setUp() {
        this.filterUnderTest = new PassThroughDetectionFilter();
        this.outlierDetectorResult = new OutlierDetectorResult();
        this.metricData = new MetricData(new MetricDefinition("DUMMY_METRIC_DEFN_KEY"), 0, 0);
    }

    @Test
    public void testFilteredWithChain() {
        doNothing().when(mockChain).doFilter(detectorRequest, detectorResponse);
        filterUnderTest.doFilter(detectorRequest, detectorResponse, mockChain);
        verify(mockChain, times(1)).doFilter(detectorRequest, detectorResponse);
    }

    @Test(expected = NullPointerException.class)
    public void testAggregate_nullRequest() {
        filterUnderTest.doFilter(null, detectorResponse, mockChain);
    }

    @Test(expected = NullPointerException.class)
    public void testAggregate_nullResponse() {
        filterUnderTest.doFilter(detectorRequest, null, mockChain);
    }

    @Test(expected = NullPointerException.class)
    public void testAggregate_nullChain() {
        filterUnderTest.doFilter(detectorRequest, detectorResponse, null);
    }

}

