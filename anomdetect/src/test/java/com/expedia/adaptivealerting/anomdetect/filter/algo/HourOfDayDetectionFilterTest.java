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

import com.expedia.adaptivealerting.anomdetect.detect.Detector;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorContainer;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorRequest;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorResponse;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorResult;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.OutlierDetectorResult;
import com.expedia.adaptivealerting.anomdetect.filter.chain.DetectionFilterChain;
import com.expedia.metrics.MetricData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.expedia.adaptivealerting.anomdetect.testutil.MetricDataHelper.buildMetricDataInsideNineToFive;
import static com.expedia.adaptivealerting.anomdetect.testutil.MetricDataHelper.buildMetricDataOutsideNineToFive;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class HourOfDayDetectionFilterTest {

    public static final double DUMMY_VALUE = 100;
    public static final MetricData METRIC_DATA_INSIDE_NINE_TO_FIVE = buildMetricDataInsideNineToFive(DUMMY_VALUE);
    public static final MetricData METRIC_DATA_OUTSIDE_NINE_TO_FIVE = buildMetricDataOutsideNineToFive(DUMMY_VALUE);
    @Mock
    private DetectorContainer mockDetectorContainer;
    @Mock
    private DetectorResult mockDetectorResult;
    @Mock
    private Detector mockDetector;
    private HourOfDayDetectionFilter filterUnderTest = new HourOfDayDetectionFilter(9, 17);

    @Before
    public void setUp() {
        initDependencies();
    }

    @Test
    public void testInsideFilteredPeriod() {
        when(mockDetector.detect(METRIC_DATA_INSIDE_NINE_TO_FIVE)).thenReturn(mockDetectorResult);
        DetectorResponse detectorResponse = new DetectorResponse();
        filterUnderTest.doFilter(new DetectorRequest(METRIC_DATA_INSIDE_NINE_TO_FIVE), detectorResponse, chain());
        DetectorResult actualResult = detectorResponse.getDetectorResult();
        assertSame(mockDetectorResult, actualResult);
    }

    @Test
    public void testOutsideFilteredPeriod() {
        DetectorResponse detectorResponse = new DetectorResponse();
        filterUnderTest.doFilter(new DetectorRequest(METRIC_DATA_OUTSIDE_NINE_TO_FIVE), detectorResponse, chain());
        OutlierDetectorResult emptyResult = new OutlierDetectorResult();
        assertEquals(emptyResult, detectorResponse.getDetectorResult());
        verify(mockDetector, never()).detect(any(MetricData.class));
    }

    @Test
    public void testValidate_sameStartAndEndHour() {
        new HourOfDayDetectionFilter(0, 0);
        new HourOfDayDetectionFilter(23, 23);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidate_startHourGreaterThan23() {
        new HourOfDayDetectionFilter(24, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidate_startHourLessThan0() {
        new HourOfDayDetectionFilter(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidate_endHourGreaterThan23() {
        new HourOfDayDetectionFilter(0, 24);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidate_endHourLessThan0() {
        new HourOfDayDetectionFilter(0, -1);
    }

    private void initDependencies() {
        when(mockDetectorContainer.getDetector()).thenReturn(mockDetector);
    }

    private DetectionFilterChain chain() {
        return new DetectionFilterChain(mockDetectorContainer);
    }

}
