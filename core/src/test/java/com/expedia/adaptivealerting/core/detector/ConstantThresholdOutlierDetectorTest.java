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

package com.expedia.adaptivealerting.core.detector;

import com.expedia.adaptivealerting.core.OutlierLevel;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;

import static com.expedia.adaptivealerting.core.detector.ConstantThresholdOutlierDetector.LEFT_TAILED;
import static com.expedia.adaptivealerting.core.detector.ConstantThresholdOutlierDetector.RIGHT_TAILED;
import static com.expedia.adaptivealerting.core.util.MetricPointUtil.metricPoint;
import static junit.framework.TestCase.assertEquals;

/**
 * @author Willie Wheeler
 */
public class ConstantThresholdOutlierDetectorTest {
    private Instant instant;
    
    @Before
    public void setUp() {
        this.instant = Instant.now();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testValidateTail() {
        new ConstantThresholdOutlierDetector(-1, 0.0, 1.0);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testValidateLeftTailedThresholds() {
        new ConstantThresholdOutlierDetector(LEFT_TAILED, 10.0, 30.0);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testValidateRightTailedThresholds() {
        new ConstantThresholdOutlierDetector(RIGHT_TAILED, 30.0, 10.0);
    }
    
    @Test
    public void testAccessors() {
        ConstantThresholdOutlierDetector detector = new ConstantThresholdOutlierDetector(RIGHT_TAILED, 30.0, 60.0);
        assertEquals(RIGHT_TAILED, detector.getTail());
        assertEquals(30.0, detector.getWeakThreshold());
        assertEquals(60.0, detector.getLargeThreshold());
    }
    
    @Test
    public void testEvaluateLeftTailed_positiveThresholds() {
        ConstantThresholdOutlierDetector detector = new ConstantThresholdOutlierDetector(LEFT_TAILED, 300.0, 100.0);
        assertEquals(OutlierLevel.NORMAL, detector.classify(metricPoint(instant, 500.0f)));
        assertEquals(OutlierLevel.WEAK, detector.classify(metricPoint(instant, 300.0f)));
        assertEquals(OutlierLevel.WEAK, detector.classify(metricPoint(instant, 200.0f)));
        assertEquals(OutlierLevel.STRONG, detector.classify(metricPoint(instant, 100.0f)));
        assertEquals(OutlierLevel.STRONG, detector.classify(metricPoint(instant, 50.0f)));
    }
    
    @Test
    public void testEvaluateLeftTailed_negativeThresholds() {
        ConstantThresholdOutlierDetector detector = new ConstantThresholdOutlierDetector(LEFT_TAILED, -10.0, -30.0);
        assertEquals(OutlierLevel.NORMAL, detector.classify(metricPoint(instant, 1.0f)));
        assertEquals(OutlierLevel.WEAK, detector.classify(metricPoint(instant, -10.0f)));
        assertEquals(OutlierLevel.WEAK, detector.classify(metricPoint(instant, -15.0f)));
        assertEquals(OutlierLevel.STRONG, detector.classify(metricPoint(instant, -30.0f)));
        assertEquals(OutlierLevel.STRONG, detector.classify(metricPoint(instant, -50.0f)));
    }
    
    @Test
    public void testEvaluateRightTailed_positiveThresholds() {
        ConstantThresholdOutlierDetector detector = new ConstantThresholdOutlierDetector(RIGHT_TAILED, 200.0, 300.0);
        assertEquals(OutlierLevel.NORMAL, detector.classify(metricPoint(instant, 100.0f)));
        assertEquals(OutlierLevel.WEAK, detector.classify(metricPoint(instant, 200.0f)));
        assertEquals(OutlierLevel.WEAK, detector.classify(metricPoint(instant, 220.0f)));
        assertEquals(OutlierLevel.STRONG, detector.classify(metricPoint(instant, 300.0f)));
        assertEquals(OutlierLevel.STRONG, detector.classify(metricPoint(instant, 8675309.0f)));
    }
    
    @Test
    public void testEvaluateRightTailed_negativeThresholds() {
        ConstantThresholdOutlierDetector detector = new ConstantThresholdOutlierDetector(RIGHT_TAILED, -300.0, -100.0);
        assertEquals(OutlierLevel.NORMAL, detector.classify(metricPoint(instant, -1000.0f)));
        assertEquals(OutlierLevel.WEAK, detector.classify(metricPoint(instant, -300.0f)));
        assertEquals(OutlierLevel.WEAK, detector.classify(metricPoint(instant, -250.0f)));
        assertEquals(OutlierLevel.STRONG, detector.classify(metricPoint(instant, -100.0f)));
        assertEquals(OutlierLevel.STRONG, detector.classify(metricPoint(instant, 0.0f)));
    }
}
