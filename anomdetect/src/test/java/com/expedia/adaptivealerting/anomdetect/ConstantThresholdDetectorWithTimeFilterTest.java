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
import com.expedia.adaptivealerting.anomdetect.detect.DetectorContainer;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorResult;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.constant.ConstantThresholdDetector;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.constant.ConstantThresholdDetectorFactoryProvider;
import com.expedia.adaptivealerting.anomdetect.source.DefaultDetectorSource;
import com.expedia.adaptivealerting.anomdetect.source.DetectorClient;
import com.expedia.adaptivealerting.anomdetect.source.DetectorFactory;
import com.expedia.metrics.MetricData;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;

import static com.expedia.adaptivealerting.anomdetect.detect.AnomalyLevel.STRONG;
import static com.expedia.adaptivealerting.anomdetect.testutil.MetricDataHelper.buildMetricDataInsideNineToFive;
import static com.expedia.adaptivealerting.anomdetect.testutil.MetricDataHelper.buildMetricDataOutsideNineToFive;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConstantThresholdDetectorWithTimeFilterTest extends AbstractDetectorFactoryTest {
    private static final UUID DETECTOR_UUID = UUID.randomUUID();
    private static final double ANOMALOUS_VALUE = 999999;
    private static final MetricData ANOMALOUS_METRIC_INSIDE_NINE_TO_FIVE = buildMetricDataInsideNineToFive(ANOMALOUS_VALUE);
    private static final MetricData ANOMALOUS_METRIC_OUTSIDE_NINE_TO_FIVE = buildMetricDataOutsideNineToFive(ANOMALOUS_VALUE);

    private DefaultDetectorSource detectorSource;

    @Mock
    private DetectorClient detectorClient;

    private DetectorFactory detectorFactory = new DetectorFactory();

    private ConstantThresholdDetectorFactoryProvider factory = new ConstantThresholdDetectorFactoryProvider();
    private DetectorExecutorImpl executor = new DetectorExecutorImpl();
    private ConstantThresholdDetector detector;
    private DetectorContainer detectorContainer;

    @Before
    public void setUp() {
        initDependencies();
        this.detectorSource = new DefaultDetectorSource(detectorClient, detectorFactory);
    }

    @Test
    public void testDetectionWithFilterOpen() {
        val detectorContainer = detectorSource.findDetector(DETECTOR_UUID);
        DetectorResult actualResult = executor.doDetection(detectorContainer, ANOMALOUS_METRIC_INSIDE_NINE_TO_FIVE);
        assertSame(STRONG, actualResult.getAnomalyLevel());
    }

    @Test
    public void testDetectionWithFilterClosed() {
        val detectorContainer = detectorSource.findDetector(DETECTOR_UUID);
        DetectorResult actualResult = executor.doDetection(detectorContainer, ANOMALOUS_METRIC_OUTSIDE_NINE_TO_FIVE);
        assertNull(actualResult.getAnomalyLevel());
    }

    private void initDependencies() {
        val document = readDocument("constant-threshold-9amTo5pm-filter");
        when(detectorClient.findDetectorDocument(DETECTOR_UUID)).thenReturn(document);
    }

}
