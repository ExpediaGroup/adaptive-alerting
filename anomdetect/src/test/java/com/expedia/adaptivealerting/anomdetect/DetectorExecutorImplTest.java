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
import com.expedia.adaptivealerting.anomdetect.detect.DetectorResult;
import com.expedia.adaptivealerting.anomdetect.detect.FilterableDetector;
import com.expedia.adaptivealerting.anomdetect.filter.PostDetectionFilter;
import com.expedia.adaptivealerting.anomdetect.filter.PreDetectionFilter;
import com.expedia.adaptivealerting.anomdetect.filter.chain.PostDetectionFilterChain;
import com.expedia.adaptivealerting.anomdetect.filter.chain.PreDetectionFilterChain;
import com.expedia.metrics.MetricData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DetectorExecutorImplTest {

    private DetectorExecutorImpl executorUnderTest = new DetectorExecutorImpl();
    @Mock
    private FilterableDetector filterableDetector;
    @Mock
    private Detector detector;
    @Mock
    private MetricData metricData;
    @Mock
    private PreDetectionFilter preDetectionFilter;
    @Mock
    private PostDetectionFilter postDetectionFilter;
    @Mock
    private DetectorResult detectorResult;

    @Test
    public void doDetectionWithFiltering() {
        when(filterableDetector.getPreDetectionFilters()).thenReturn(singletonList(preDetectionFilter));
        when(filterableDetector.getPostDetectionFilters()).thenReturn(singletonList(postDetectionFilter));
        when(preDetectionFilter.doFilter(same(metricData), any(PreDetectionFilterChain.class))).thenReturn(detectorResult);
        when(postDetectionFilter.doFilter(same(detectorResult), any(PostDetectionFilterChain.class))).thenReturn(detectorResult);
        DetectorResult actualResult = executorUnderTest.doDetectionWithOptionalFiltering(filterableDetector, metricData);
        assertSame(this.detectorResult, actualResult);
    }

    @Test(expected = IllegalArgumentException.class)
    public void doDetectionWithFiltering_failsWithNullPreFilterList() {
        when(filterableDetector.getPreDetectionFilters()).thenReturn(null);
        executorUnderTest.doDetectionWithOptionalFiltering(filterableDetector, metricData);
    }

    @Test(expected = IllegalArgumentException.class)
    public void doDetectionWithFiltering_failsWithNullPostFilterList() {
        when(filterableDetector.getPostDetectionFilters()).thenReturn(null);
        executorUnderTest.doDetectionWithOptionalFiltering(filterableDetector, metricData);
    }

    @Test
    public void doDetectionWithNoFiltering() {
        when(detector.detect(metricData)).thenReturn(detectorResult);
        DetectorResult actualResult = executorUnderTest.doDetectionWithOptionalFiltering(detector, metricData);
        assertSame(this.detectorResult, actualResult);
    }
}
