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
import org.junit.Test;

import static com.expedia.adaptivealerting.core.util.MathUtil.isApproximatelyEqual;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * @author Willie Wheeler
 */
public class EwmaAnomalyDetectorTests {
    private static final double TOLERANCE = 0.01;
    
    @Test
    public void testEvaluate() {
        EwmaAnomalyDetector detector = new EwmaAnomalyDetector(0.2, 2.0, 3.0, 300.0);
        
        assertEquals(0.2, detector.getAlpha());
        assertEquals(2.0, detector.getSmallMultiplier());
        assertEquals(3.0, detector.getLargeMultiplier());
        assertEquals(300.0, detector.getMean());
        assertEquals(0.0, detector.getVariance());
        
        AnomalyLevel actualLevel = detector.evaluate(305.0);
        assertEquals(AnomalyLevel.LARGE, actualLevel);
    }
    
    private static void assertApproxEqual(double d1, double d2) {
        assertTrue(isApproximatelyEqual(d1, d2, TOLERANCE));
    }
}
