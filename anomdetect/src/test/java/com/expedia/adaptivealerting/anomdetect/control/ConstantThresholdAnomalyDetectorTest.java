/*
 * Copyright 2018 Expedia Group, Inc.
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

package com.expedia.adaptivealerting.anomdetect.control;

import com.expedia.adaptivealerting.anomdetect.AnomalyDetector;
import com.expedia.adaptivealerting.anomdetect.control.ConstantThresholdAnomalyDetector;
import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.util.MetricUtil;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Willie Wheeler
 */
public class ConstantThresholdAnomalyDetectorTest {
    private Instant instant;
    
    @Before
    public void setUp() {
        this.instant = Instant.now();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testValidateTail() {
        new ConstantThresholdAnomalyDetector(-1, 1.0, 0.0);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testValidateLeftTailedThresholds() {
        new ConstantThresholdAnomalyDetector(ConstantThresholdAnomalyDetector.LEFT_TAILED, 30.0, 10.0);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testValidateRightTailedThresholds() {
        new ConstantThresholdAnomalyDetector(ConstantThresholdAnomalyDetector.RIGHT_TAILED, 10.0, 30.0);
    }
    
    @Test
    public void testAccessors() {
        ConstantThresholdAnomalyDetector detector = new ConstantThresholdAnomalyDetector(ConstantThresholdAnomalyDetector.RIGHT_TAILED, 60.0, 30.0);
        assertEquals(ConstantThresholdAnomalyDetector.RIGHT_TAILED, detector.getTail());
        assertEquals(30.0, detector.getWeakThreshold());
        assertEquals(60.0, detector.getStrongThreshold());
    }
    
    @Test
    public void testEvaluateLeftTailed_positiveThresholds() {
        ConstantThresholdAnomalyDetector detector = new ConstantThresholdAnomalyDetector(ConstantThresholdAnomalyDetector.LEFT_TAILED, 100.0, 300.0);
        assertEquals(AnomalyLevel.NORMAL, level(detector, instant, 500.0f));
        assertEquals(AnomalyLevel.WEAK, level(detector, instant, 300.0f));
        assertEquals(AnomalyLevel.WEAK, level(detector, instant, 200.0f));
        assertEquals(AnomalyLevel.STRONG, level(detector, instant, 100.0f));
        assertEquals(AnomalyLevel.STRONG, level(detector, instant, 50.0f));
    }
    
    @Test
    public void testEvaluateLeftTailed_negativeThresholds() {
        ConstantThresholdAnomalyDetector detector = new ConstantThresholdAnomalyDetector(ConstantThresholdAnomalyDetector.LEFT_TAILED, -30.0, -10.0);
        assertEquals(AnomalyLevel.NORMAL, level(detector, instant, 1.0f));
        assertEquals(AnomalyLevel.WEAK, level(detector, instant, -10.0f));
        assertEquals(AnomalyLevel.WEAK, level(detector, instant, -15.0f));
        assertEquals(AnomalyLevel.STRONG, level(detector, instant, -30.0f));
        assertEquals(AnomalyLevel.STRONG, level(detector, instant, -50.0f));
    }
    
    @Test
    public void testEvaluateRightTailed_positiveThresholds() {
        ConstantThresholdAnomalyDetector detector = new ConstantThresholdAnomalyDetector(ConstantThresholdAnomalyDetector.RIGHT_TAILED, 300.0, 200.0);
        assertEquals(AnomalyLevel.NORMAL, level(detector, instant, 100.0f));
        assertEquals(AnomalyLevel.WEAK, level(detector, instant, 200.0f));
        assertEquals(AnomalyLevel.WEAK, level(detector, instant, 220.0f));
        assertEquals(AnomalyLevel.STRONG, level(detector, instant, 300.0f));
        assertEquals(AnomalyLevel.STRONG, level(detector, instant, 8675309.0f));
    }
    
    @Test
    public void testEvaluateRightTailed_negativeThresholds() {
        ConstantThresholdAnomalyDetector detector = new ConstantThresholdAnomalyDetector(ConstantThresholdAnomalyDetector.RIGHT_TAILED, -100.0, -300.0);
        assertEquals(AnomalyLevel.NORMAL, level(detector, instant, -1000.0f));
        assertEquals(AnomalyLevel.WEAK, level(detector, instant, -300.0f));
        assertEquals(AnomalyLevel.WEAK, level(detector, instant, -250.0f));
        assertEquals(AnomalyLevel.STRONG, level(detector, instant, -100.0f));
        assertEquals(AnomalyLevel.STRONG, level(detector, instant, 0.0f));
    }
    
    private AnomalyLevel level(AnomalyDetector detector, Instant instant, float value) {
        return detector.classify(MetricUtil.metricPoint(instant.getEpochSecond(), value)).getAnomalyLevel();
    }
}
