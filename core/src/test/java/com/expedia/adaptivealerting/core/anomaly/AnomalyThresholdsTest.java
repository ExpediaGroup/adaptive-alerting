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
package com.expedia.adaptivealerting.core.anomaly;

import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AnomalyThresholdsTest {
    private static final double TOLERANCE = 0.001;

    @Test
    public void forCoverage() {
        val thresholds = new AnomalyThresholds(100.0, 90.0, 20.0, 10.0);

        assertEquals(100.0, thresholds.getUpperStrong(), TOLERANCE);
        assertEquals(90.0, thresholds.getUpperWeak(), TOLERANCE);
        assertEquals(20.0, thresholds.getLowerWeak(), TOLERANCE);
        assertEquals(10.0, thresholds.getLowerStrong(), TOLERANCE);

        thresholds.setUpperStrong(200.0);
        thresholds.setUpperWeak(180.0);
        thresholds.setLowerWeak(40.0);
        thresholds.setLowerStrong(20.0);

        assertEquals(200.0, thresholds.getUpperStrong(), TOLERANCE);
        assertEquals(180.0, thresholds.getUpperWeak(), TOLERANCE);
        assertEquals(40.0, thresholds.getLowerWeak(), TOLERANCE);
        assertEquals(20.0, thresholds.getLowerStrong(), TOLERANCE);
    }

    @Test
    public void testUpperThresholds() {
        val thresholds = new AnomalyThresholds(100.0, 50.0, null, null);
        assertEquals(AnomalyLevel.STRONG, thresholds.classify(150.0));
        assertEquals(AnomalyLevel.WEAK, thresholds.classify(75.0));
        assertEquals(AnomalyLevel.NORMAL, thresholds.classify(25.0));
    }

    @Test
    public void testLowerThresholds() {
        val thresholds = new AnomalyThresholds(null, null, 50.0, 25.0);
        assertEquals(AnomalyLevel.STRONG, thresholds.classify(0.0));
        assertEquals(AnomalyLevel.WEAK, thresholds.classify(35.0));
        assertEquals(AnomalyLevel.NORMAL, thresholds.classify(100.0));
    }
}
