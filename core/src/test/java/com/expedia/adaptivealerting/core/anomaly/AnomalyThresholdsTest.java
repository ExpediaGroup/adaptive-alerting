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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AnomalyThresholdsTest {
    
    @Test
    public void testUpperThresholds() {
        final AnomalyThresholds thresholds = new AnomalyThresholds(100.0, 50.0, null, null);
        assertEquals(AnomalyLevel.STRONG, thresholds.classify(150.0));
        assertEquals(AnomalyLevel.WEAK, thresholds.classify(75.0));
        assertEquals(AnomalyLevel.NORMAL, thresholds.classify(25.0));
    }
    
    @Test
    public void testLowerThresholds() {
        final AnomalyThresholds thresholds = new AnomalyThresholds(null, null, 50.0, 25.0);
        assertEquals(AnomalyLevel.STRONG, thresholds.classify(0.0));
        assertEquals(AnomalyLevel.WEAK, thresholds.classify(35.0));
        assertEquals(AnomalyLevel.NORMAL, thresholds.classify(100.0));
    }
}
