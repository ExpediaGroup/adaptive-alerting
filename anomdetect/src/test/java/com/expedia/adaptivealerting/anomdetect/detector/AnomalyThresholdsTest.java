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
package com.expedia.adaptivealerting.anomdetect.detector;

import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AnomalyThresholdsTest {
    private static final double TOLERANCE = 0.001;

    @Test
    public void testConstructor_happyPath() {
        val thresholds = new AnomalyThresholds(100.0, 90.0, 20.0, 10.0);
        assertEquals(100.0, thresholds.getUpperStrong(), TOLERANCE);
        assertEquals(90.0, thresholds.getUpperWeak(), TOLERANCE);
        assertEquals(20.0, thresholds.getLowerWeak(), TOLERANCE);
        assertEquals(10.0, thresholds.getLowerStrong(), TOLERANCE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_allThresholdsNull() {
        new AnomalyThresholds(null, null, null, null);
    }

    @Test
    public void testConstructor_upperStrongOnly() {
        new AnomalyThresholds(100.0, null, null, null);
    }

    @Test
    public void testConstructor_upperWeakOnly() {
        new AnomalyThresholds(null, 100.0, null, null);
    }

    @Test
    public void testConstructor_lowerWeakOnly() {
        new AnomalyThresholds(null, null, 100.0, null);
    }

    @Test
    public void testConstructor_lowerStrongOnly() {
        new AnomalyThresholds(null, null, null, 100.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_upperStrongVsUpperWeakConflict() {
        new AnomalyThresholds(90.0, 100.0, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_upperStrongVsLowerWeakConflict() {
        new AnomalyThresholds(90.0, null, 100.0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_upperStrongVsLowerStrongConflict() {
        new AnomalyThresholds(90.0, null, null, 100.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_upperWeakVsLowerWeakConflict() {
        new AnomalyThresholds(null, 90.0, 100.0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_upperWeakVsLowerStrongConflict() {
        new AnomalyThresholds(null, 90.0, null, 100.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_lowerWeakVsLowerStrongConflict() {
        new AnomalyThresholds(null, null, 90.0, 100.0);
    }
}
