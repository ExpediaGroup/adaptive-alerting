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

package com.expedia.adaptivealerting.core.util;

import org.junit.Test;

import static com.expedia.adaptivealerting.core.util.AssertUtil.isBetween;
import static com.expedia.adaptivealerting.core.util.AssertUtil.isTrue;
import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * @author Willie Wheeler
 */
public class AssertUtilTest {
    
    @Test
    public void testIsTrue_trueValue() {
        // Expect this to work without throwing an exception
        isTrue(true, "The param must be true");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testIsTrue_falseValue() {
        isTrue(false, "The param must be true");
    }
    
    @Test
    public void testNotNull_trueValue() {
        notNull("foo", "The param must be null");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNotNull_falseValue() {
        notNull(null, "The param must be null");
    }
    
    @Test
    public void testIsBetween_trueValue() {
        isBetween(3.0, 0.0, 5.0, "Blah blah blah");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testIsBetween_falseValue() {
        isBetween(-3.0, 0.0, 5.0, "Blah blah blah");
    }
}
