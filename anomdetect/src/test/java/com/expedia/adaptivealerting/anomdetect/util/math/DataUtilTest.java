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
package com.expedia.adaptivealerting.anomdetect.util.math;

import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class DataUtilTest {
    private static final double TOLERANCE = 0.001;

    @Test
    public void testUnitScale_constant() {
        val data = new double[] { 3.0, 3.0 };
        val scaled = DataUtil.unitScale(data);
        assertArrayEquals(new double[] { 0.0, 0.0 }, scaled, TOLERANCE);
    }

    @Test
    public void testUnitScale_binary() {
        val data = new double[] { 4.0, 6.5 };
        val scaled = DataUtil.unitScale(data);
        assertArrayEquals(new double[] { 0.0, 1.0 }, scaled, TOLERANCE);
    }

    @Test
    public void testUnitScale() {
        val data0 = new double[] { -3.0, 6.0, 0.0 };
        val scaled0 = DataUtil.unitScale(data0);
        assertArrayEquals(new double[] { 0.0, 1.0, 0.333 }, scaled0, TOLERANCE);

        val data1 = new double[] { 25.0, 50.0, 75.0, 0.0, 100.0 };
        val scaled1 = DataUtil.unitScale(data1);
        assertArrayEquals(new double[] { 0.25, 0.50, 0.75, 0.0, 1.0 }, scaled1, TOLERANCE);
    }
}
