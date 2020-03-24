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
package com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.constant;

import com.expedia.adaptivealerting.anomdetect.detect.AnomalyLevel;
import com.expedia.adaptivealerting.anomdetect.detect.AnomalyThresholds;
import com.expedia.adaptivealerting.anomdetect.detect.AnomalyType;
import com.expedia.adaptivealerting.anomdetect.detect.Detector;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.OutlierDetectorResult;
import com.expedia.adaptivealerting.anomdetect.filter.DetectionFilters;
import com.expedia.adaptivealerting.anomdetect.filter.PostDetectionFilter;
import com.expedia.adaptivealerting.anomdetect.filter.PreDetectionFilter;
import com.expedia.adaptivealerting.anomdetect.filter.algo.post.PassThroughPostDetectionFilter;
import com.expedia.adaptivealerting.anomdetect.filter.algo.pre.PassThroughPreDetectionFilter;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.google.common.collect.ImmutableList;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class ConstantThresholdDetectorTest {
    private UUID detectorUuid;
    private MetricDefinition metricDefinition;
    private long epochSecond;
    private boolean trusted;

    @Mock
    private AnomalyThresholds thresholds;
    @Mock
    private DetectionFilters detectionFilters;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.detectorUuid = UUID.randomUUID();
        this.metricDefinition = new MetricDefinition("some-key");
        this.epochSecond = Instant.now().getEpochSecond();
    }

    @Test
    public void testAccessors() {
        ImmutableList<PreDetectionFilter> preDetectionFilters = ImmutableList.of(new PassThroughPreDetectionFilter());
        ImmutableList<PostDetectionFilter> postDetectionFilters = ImmutableList.of(new PassThroughPostDetectionFilter());
        DetectionFilters filters = new DetectionFilters(preDetectionFilters, postDetectionFilters);
        val detector = detector(detectorUuid, thresholds, AnomalyType.LEFT_TAILED, trusted, filters);
        assertEquals(detectorUuid, detector.getUuid());
        assertSame(thresholds, detector.getParams().getThresholds());
        assertEquals(trusted, detector.isTrusted());
        assertSame(preDetectionFilters, detector.getPreDetectionFilters());
        assertSame(postDetectionFilters, detector.getPostDetectionFilters());
    }

    @Test
    public void testEvaluateLeftTailed_positiveThresholds() {
        val thresholds = new AnomalyThresholds(null, null, 300.0, 100.0);
        val detector = detector(detectorUuid, thresholds, AnomalyType.LEFT_TAILED, trusted, detectionFilters);
        verifyResult(AnomalyLevel.NORMAL, detector, epochSecond, 500.0);
        verifyResult(AnomalyLevel.NORMAL, detector, epochSecond, 300.0);
        verifyResult(AnomalyLevel.WEAK, detector, epochSecond, 200.0);
        verifyResult(AnomalyLevel.WEAK, detector, epochSecond, 100.0);
        verifyResult(AnomalyLevel.STRONG, detector, epochSecond, 50.0);
        verifyResult(AnomalyLevel.STRONG, detector, epochSecond, 0.0);
        verifyResult(AnomalyLevel.STRONG, detector, epochSecond, -50.0);
    }

    @Test
    public void testEvaluateLeftTailed_negativeThresholds() {
        val thresholds = new AnomalyThresholds(null, null, -10.0, -30.0);
        val detector = detector(detectorUuid, thresholds, AnomalyType.LEFT_TAILED, trusted, detectionFilters);
        verifyResult(AnomalyLevel.NORMAL, detector, epochSecond, 1.0);
        verifyResult(AnomalyLevel.NORMAL, detector, epochSecond, -10.0);
        verifyResult(AnomalyLevel.WEAK, detector, epochSecond, -15.0);
        verifyResult(AnomalyLevel.WEAK, detector, epochSecond, -30.0);
        verifyResult(AnomalyLevel.STRONG, detector, epochSecond, -50.0);
    }

    @Test
    public void testEvaluateRightTailed_positiveThresholds() {
        val thresholds = new AnomalyThresholds(300.0, 200.0, null, null);
        val detector = detector(detectorUuid, thresholds, AnomalyType.RIGHT_TAILED, trusted, detectionFilters);
        verifyResult(AnomalyLevel.NORMAL, detector, epochSecond, 100.0);
        verifyResult(AnomalyLevel.NORMAL, detector, epochSecond, 200.0);
        verifyResult(AnomalyLevel.WEAK, detector, epochSecond, 220.0);
        verifyResult(AnomalyLevel.WEAK, detector, epochSecond, 300.0);
        verifyResult(AnomalyLevel.STRONG, detector, epochSecond, 8675309.0);
    }

    @Test
    public void testEvaluateRightTailed_negativeThresholds() {
        val thresholds = new AnomalyThresholds(-100.0, -300.0, null, null);
        val detector = detector(detectorUuid, thresholds, AnomalyType.RIGHT_TAILED, trusted, detectionFilters);
        verifyResult(AnomalyLevel.NORMAL, detector, epochSecond, -300.0);
        verifyResult(AnomalyLevel.WEAK, detector, epochSecond, -250.0);
        verifyResult(AnomalyLevel.WEAK, detector, epochSecond, -100.0);
        verifyResult(AnomalyLevel.STRONG, detector, epochSecond, 0.0);
        verifyResult(AnomalyLevel.STRONG, detector, epochSecond, 1000.0);
    }

    @Test
    public void testEvaluateTwoTailed_zeroThresholds() {
        val thresholds = new AnomalyThresholds(0.0, 0.0, 0.0, 0.0);
        val detector = detector(detectorUuid, thresholds, AnomalyType.TWO_TAILED, trusted, detectionFilters);
        verifyResult(AnomalyLevel.STRONG, detector, epochSecond, 1.0);
        verifyResult(AnomalyLevel.NORMAL, detector, epochSecond, 0.0);
        verifyResult(AnomalyLevel.STRONG, detector, epochSecond, -1.0);
    }

    @Test
    public void testDetectionWithHourFilter() {
        val thresholds = new AnomalyThresholds(0.0, 0.0, 0.0, 0.0);
        val detector = detector(detectorUuid, thresholds, AnomalyType.TWO_TAILED, trusted, detectionFilters);
        verifyResult(AnomalyLevel.STRONG, detector, epochSecond, 1.0);
        verifyResult(AnomalyLevel.NORMAL, detector, epochSecond, 0.0);
        verifyResult(AnomalyLevel.STRONG, detector, epochSecond, -1.0);
    }

    @Test
    public void testName() {
        val detector = detector(detectorUuid, thresholds, AnomalyType.LEFT_TAILED, trusted, detectionFilters);
        assertNotNull(detector.getName());
        assertEquals("constant-threshold", detector.getName());
    }

    private ConstantThresholdDetector detector(UUID uuid, AnomalyThresholds thresholds, AnomalyType type, boolean trusted, DetectionFilters filters) {
        val params = new ConstantThresholdDetectorParams().setThresholds(thresholds).setType(type);
        return new ConstantThresholdDetector(uuid, params, trusted, filters);
    }

    private void verifyResult(AnomalyLevel level, Detector detector, long epochSecond, double value) {
        val metricData = new MetricData(metricDefinition, value, epochSecond);
        val result = (OutlierDetectorResult) detector.detect(metricData);
        assertNull(result.getPredicted());
        assertNotNull(result.getThresholds());
        assertEquals(level, result.getAnomalyLevel());
    }
}
