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

import org.junit.Test;

import static com.expedia.adaptivealerting.core.util.MathUtil.isApproximatelyEqual;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class MathUtilTest {

    @Test
    public void testIsApproximatelyEqual() {
        assertTrue(isApproximatelyEqual(2.0, 2.0, 0.0));
        assertTrue(isApproximatelyEqual(2.0, 2.0, 0.1));

        // This is surprising, but it's because floating point is only approximate.
        assertFalse(isApproximatelyEqual(2.0, 2.1, 0.1));

        assertTrue(isApproximatelyEqual(2.0, 2.1, 0.11));
    }
}
