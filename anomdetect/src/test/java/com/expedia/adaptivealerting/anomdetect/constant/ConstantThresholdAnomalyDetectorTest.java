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
package com.expedia.adaptivealerting.anomdetect.constant;

import com.expedia.adaptivealerting.anomdetect.AnomalyDetector;
import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyThresholds;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @author Willie Wheeler
 */
public class ConstantThresholdAnomalyDetectorTest {
    private UUID detectorUUID;
    private MetricDefinition metricDefinition;
    private long epochSecond;
    
    @Mock
    private ConstantThresholds thresholds;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.detectorUUID = UUID.randomUUID();
        this.metricDefinition = new MetricDefinition("some-key");
        this.epochSecond = Instant.now().getEpochSecond();
    }
    
    @Test
    public void testAccessors() {
        ConstantThresholdAnomalyDetector detector = detector(detectorUUID, thresholds, ConstantThresholdParams.Type.LEFT_TAILED);
        assertSame(thresholds, detector.getParams().getThresholds());
    }
    
    @Test
    public void testEvaluateLeftTailed_positiveThresholds() {
        ConstantThresholds thresholds = new ConstantThresholds(null, null, 100.0, 300.0);
        ConstantThresholdAnomalyDetector detector = detector(detectorUUID, thresholds, ConstantThresholdParams.Type.LEFT_TAILED);
        assertEquals(AnomalyLevel.NORMAL, classify(detector, epochSecond, 500.0f));
        assertEquals(AnomalyLevel.WEAK, classify(detector, epochSecond, 300.0f));
        assertEquals(AnomalyLevel.WEAK, classify(detector,epochSecond, 200.0f));
        assertEquals(AnomalyLevel.STRONG, classify(detector,epochSecond, 100.0f));
        assertEquals(AnomalyLevel.STRONG, classify(detector,epochSecond, 50.0f));
    }
    
    @Test
    public void testEvaluateLeftTailed_negativeThresholds() {
        ConstantThresholds thresholds = new ConstantThresholds(null, null, -30.0, -10.0);
        ConstantThresholdAnomalyDetector detector = detector(detectorUUID, thresholds, ConstantThresholdParams.Type.LEFT_TAILED);
        assertEquals(AnomalyLevel.NORMAL, classify(detector,epochSecond, 1.0f));
        assertEquals(AnomalyLevel.WEAK, classify(detector,epochSecond, -10.0f));
        assertEquals(AnomalyLevel.WEAK, classify(detector,epochSecond, -15.0f));
        assertEquals(AnomalyLevel.STRONG, classify(detector,epochSecond, -30.0f));
        assertEquals(AnomalyLevel.STRONG, classify(detector,epochSecond, -50.0f));
    }
    
    @Test
    public void testEvaluateRightTailed_positiveThresholds() {
        ConstantThresholds thresholds = new ConstantThresholds(300.0, 200.0, null, null);
        ConstantThresholdAnomalyDetector detector = detector(detectorUUID, thresholds, ConstantThresholdParams.Type.RIGHT_TAILED);
        assertEquals(AnomalyLevel.NORMAL, classify(detector,epochSecond, 100.0f));
        assertEquals(AnomalyLevel.WEAK, classify(detector,epochSecond, 200.0f));
        assertEquals(AnomalyLevel.WEAK, classify(detector,epochSecond, 220.0f));
        assertEquals(AnomalyLevel.STRONG, classify(detector,epochSecond, 300.0f));
        assertEquals(AnomalyLevel.STRONG, classify(detector,epochSecond, 8675309.0f));
    }
    
    @Test
    public void testEvaluateRightTailed_negativeThresholds() {
        ConstantThresholds thresholds = new ConstantThresholds(-100.0, -300.0, null, null);
        ConstantThresholdAnomalyDetector detector = detector(detectorUUID, thresholds, ConstantThresholdParams.Type.RIGHT_TAILED);
        assertEquals(AnomalyLevel.NORMAL, classify(detector,epochSecond, -1000.0f));
        assertEquals(AnomalyLevel.WEAK, classify(detector,epochSecond, -300.0f));
        assertEquals(AnomalyLevel.WEAK, classify(detector,epochSecond, -250.0f));
        assertEquals(AnomalyLevel.STRONG, classify(detector,epochSecond, -100.0f));
        assertEquals(AnomalyLevel.STRONG, classify(detector,epochSecond, 0.0f));
    }
    
    private ConstantThresholdAnomalyDetector detector(UUID uuid, ConstantThresholds thresholds, ConstantThresholdParams.Type type) {
        ConstantThresholdParams params = new ConstantThresholdParams()
                .setThresholds(thresholds).setType(type);
        return new ConstantThresholdAnomalyDetector(uuid, params);
    }
    
    private AnomalyLevel classify(AnomalyDetector detector, long epochSecond, float value) {
        return detector.classify(metricData(epochSecond, value)).getAnomalyLevel();
    }
    
    private MetricData metricData(long epochSecond, float value) {
        return new MetricData(metricDefinition, value, epochSecond);
    }
}
