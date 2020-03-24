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

import com.expedia.adaptivealerting.anomdetect.detect.AbstractDetectorFactoryTest;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorResult;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.constant.ConstantThresholdDetector;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.constant.ConstantThresholdDetectorFactoryProvider;
import com.expedia.metrics.MetricData;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static com.expedia.adaptivealerting.anomdetect.detect.AnomalyLevel.STRONG;
import static com.expedia.adaptivealerting.anomdetect.testutil.MetricDataHelper.buildMetricDataInsideNineToFive;
import static com.expedia.adaptivealerting.anomdetect.testutil.MetricDataHelper.buildMetricDataOutsideNineToFive;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

@RunWith(MockitoJUnitRunner.class)
public class ConstantThresholdDetectorWithTimeFilterTest extends AbstractDetectorFactoryTest {
    private static final double ANOMALOUS_VALUE = 999999;
    private static final MetricData ANOMALOUS_METRIC_INSIDE_NINE_TO_FIVE = buildMetricDataInsideNineToFive(ANOMALOUS_VALUE);
    private static final MetricData ANOMALOUS_METRIC_OUTSIDE_NINE_TO_FIVE = buildMetricDataOutsideNineToFive(ANOMALOUS_VALUE);

    private ConstantThresholdDetectorFactoryProvider factory = new ConstantThresholdDetectorFactoryProvider();
    private DetectorExecutorImpl executor = new DetectorExecutorImpl();
    private ConstantThresholdDetector detector;

    @Before
    public void setUp() {
        val document = readDocument("constant-threshold-9amTo5pm-filter");
        this.detector = factory.buildDetector(document);
    }

    @Test
    public void testDetectionWithFilterOpen() {
        DetectorResult actualResult = executor.doDetectionWithOptionalFiltering(detector, ANOMALOUS_METRIC_INSIDE_NINE_TO_FIVE);
        assertSame(STRONG, actualResult.getAnomalyLevel());
    }

    @Test
    public void testDetectionWithFilterClosed() {
        DetectorResult actualResult = executor.doDetectionWithOptionalFiltering(detector, ANOMALOUS_METRIC_OUTSIDE_NINE_TO_FIVE);
        assertNull(actualResult.getAnomalyLevel());
    }

}
