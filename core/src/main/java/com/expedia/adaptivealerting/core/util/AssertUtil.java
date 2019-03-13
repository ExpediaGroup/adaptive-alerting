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
package com.expedia.adaptivealerting.core.util;

/**
 * Assertion utilities.
 */
public class AssertUtil {

    /**
     * Prevent instantiation.
     */
    private AssertUtil() {
    }

    public static void isTrue(boolean b, String message) {
        if (!b) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isFalse(boolean b, String message) {
        isTrue(!b, message);
    }

    public static void notNull(Object o, String message) {
        isTrue(o != null, message);
    }

    public static void isEqual(int v1, int v2, String message) {
        isTrue(v1 == v2, message);
    }

    public static void isBetween(double value, double lowerBd, double upperBd, String message) {
        isTrue(lowerBd <= value && value <= upperBd, message);
    }
}
