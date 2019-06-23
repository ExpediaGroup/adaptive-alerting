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
package com.expedia.adaptivealerting.anomdetect.util;

import lombok.val;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class ArrayUtilTest {
    private static final double TOLERANCE = 0.001;

    @Test
    public void testShuffle() {
        val n = 14;

        val orig = new double[n];
        for (int i = 0; i < n; i++) {
            orig[i] = i;
        }

        val copy = Arrays.copyOf(orig, n);
        ArrayUtil.shuffle(copy);

        int diffCount = 0;
        for (int i = 0; i < n; i++) {
            if (orig[i] != copy[i]) {
                diffCount++;
            }
        }

        // With n = 14 (see above), there are over 87T permutations.
        // So we can safely assume that the input and output are unequal.
        assertTrue(diffCount > 0);

        Arrays.sort(copy);
        assertArrayEquals(orig, copy, TOLERANCE);
    }
}
