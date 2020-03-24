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
package com.expedia.adaptivealerting.anomdetect.filter.algo.pre;

import com.expedia.adaptivealerting.anomdetect.detect.outlier.OutlierDetectorResult;
import com.expedia.adaptivealerting.anomdetect.filter.chain.PreDetectionFilterChain;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import lombok.val;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PassThroughPreDetectionFilterTest {
    private PassThroughPreDetectionFilter filterUnderTest;
    private OutlierDetectorResult outlierDetectorResult;
    private MetricData metricData;
    @Mock
    private PreDetectionFilterChain mockChain;

    @Before
    public void setUp() {
        this.filterUnderTest = new PassThroughPreDetectionFilter();
        this.outlierDetectorResult = new OutlierDetectorResult();
        this.metricData = new MetricData(new MetricDefinition("DUMMY_METRIC_DEFN_KEY"), 0, 0);
    }

    @Test
    public void testFilteredWithChain() {
        when(mockChain.doFilter(metricData)).thenReturn(outlierDetectorResult);
        val filteredResult = filterUnderTest.doFilter(metricData, mockChain);
        Assert.assertSame(outlierDetectorResult, filteredResult);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAggregate_nullMetricData() {
        filterUnderTest.doFilter(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAggregate_nullChain() {
        filterUnderTest.doFilter(metricData, null);
    }

}

