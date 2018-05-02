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

package com.expedia.adaptivealerting.core.model;

import com.expedia.adaptivealerting.core.AnomalyLevel;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;

import static com.expedia.adaptivealerting.core.model.ConstantThresholdAnomalyDetector.LEFT_TAILED;
import static com.expedia.adaptivealerting.core.model.ConstantThresholdAnomalyDetector.RIGHT_TAILED;
import static junit.framework.TestCase.assertEquals;

/**
 * @author Willie Wheeler
 */
public class ConstantThresholdAnomalyDetectorTests {
    private Instant instant;
    
    @Before
    public void setUp() {
        this.instant = Instant.now();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testValidateTail() {
        new ConstantThresholdAnomalyDetector(-1, 0.0, 1.0);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testValidateLeftTailedThresholds() {
        new ConstantThresholdAnomalyDetector(LEFT_TAILED, 10.0, 30.0);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testValidateRightTailedThresholds() {
        new ConstantThresholdAnomalyDetector(RIGHT_TAILED, 30.0, 10.0);
    }
    
    @Test
    public void testAccessors() {
        ConstantThresholdAnomalyDetector detector = new ConstantThresholdAnomalyDetector(RIGHT_TAILED, 30.0, 60.0);
        assertEquals(RIGHT_TAILED, detector.getTail());
        assertEquals(30.0, detector.getSmallThreshold());
        assertEquals(60.0, detector.getLargeThreshold());
    }
    
    @Test
    public void testEvaluateLeftTailed_positiveThresholds() {
        ConstantThresholdAnomalyDetector detector = new ConstantThresholdAnomalyDetector(LEFT_TAILED, 300.0, 100.0);
        assertEquals(AnomalyLevel.NORMAL, detector.evaluate(instant, 500.0));
        assertEquals(AnomalyLevel.SMALL, detector.evaluate(instant, 300.0));
        assertEquals(AnomalyLevel.SMALL, detector.evaluate(instant, 200.0));
        assertEquals(AnomalyLevel.LARGE, detector.evaluate(instant, 100.0));
        assertEquals(AnomalyLevel.LARGE, detector.evaluate(instant, 50.0));
    }
    
    @Test
    public void testEvaluateLeftTailed_negativeThresholds() {
        ConstantThresholdAnomalyDetector detector = new ConstantThresholdAnomalyDetector(LEFT_TAILED, -10.0, -30.0);
        assertEquals(AnomalyLevel.NORMAL, detector.evaluate(instant, 1.0));
        assertEquals(AnomalyLevel.SMALL, detector.evaluate(instant, -10.0));
        assertEquals(AnomalyLevel.SMALL, detector.evaluate(instant, -15.0));
        assertEquals(AnomalyLevel.LARGE, detector.evaluate(instant, -30.0));
        assertEquals(AnomalyLevel.LARGE, detector.evaluate(instant, -50.0));
    }
    
    @Test
    public void testEvaluateRightTailed_positiveThresholds() {
        ConstantThresholdAnomalyDetector detector = new ConstantThresholdAnomalyDetector(RIGHT_TAILED, 200.0, 300.0);
        assertEquals(AnomalyLevel.NORMAL, detector.evaluate(instant, 100.0));
        assertEquals(AnomalyLevel.SMALL, detector.evaluate(instant, 200.0));
        assertEquals(AnomalyLevel.SMALL, detector.evaluate(instant, 220.0));
        assertEquals(AnomalyLevel.LARGE, detector.evaluate(instant, 300.0));
        assertEquals(AnomalyLevel.LARGE, detector.evaluate(instant, 8675309.0));
    }
    
    @Test
    public void testEvaluateRightTailed_negativeThresholds() {
        ConstantThresholdAnomalyDetector detector = new ConstantThresholdAnomalyDetector(RIGHT_TAILED, -300.0, -100.0);
        assertEquals(AnomalyLevel.NORMAL, detector.evaluate(instant, -1000.0));
        assertEquals(AnomalyLevel.SMALL, detector.evaluate(instant, -300.0));
        assertEquals(AnomalyLevel.SMALL, detector.evaluate(instant, -250.0));
        assertEquals(AnomalyLevel.LARGE, detector.evaluate(instant, -100.0));
        assertEquals(AnomalyLevel.LARGE, detector.evaluate(instant, 0.0));
    }
}
