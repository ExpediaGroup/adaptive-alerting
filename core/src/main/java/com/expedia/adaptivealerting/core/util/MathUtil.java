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

/**
 * Math utilities.
 *
 * @author Willie Wheeler
 */
public final class MathUtil {
    
    /**
     * Prevent instantiation.
     */
    private MathUtil() {
    }
    
    /**
     * Indicates whether the distance between d1 and d2 is less than or equal to the given threshold.
     *
     * @param d1
     *            Value 1
     * @param d2
     *            Value 2
     * @param threshold
     *            Threshold
     * @return Boolean indicating whether the two value are approximately equal
     */
    public static boolean isApproximatelyEqual(double d1, double d2, double threshold) {
        return Math.abs(d1 - d2) <= threshold;
    }
    
}
