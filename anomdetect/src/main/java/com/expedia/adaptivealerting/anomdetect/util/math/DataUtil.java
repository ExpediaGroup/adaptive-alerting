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

import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.Arrays;

/**
 * Utility class for common data manipulation operations.
 */
@UtilityClass
public class DataUtil {

    /**
     * Scales the data to the interval [0, 1].
     *
     * @param data unscaled data
     * @return data scaled to [0, 1]
     */
    public static double[] unitScale(double[] data) {
        val summaryStats = Arrays.stream(data).summaryStatistics();
        val min = summaryStats.getMin();
        val max = summaryStats.getMax();
        val range = max - min;
        val denom = (range == 0.0 ? 1.0 : range);

        return Arrays.stream(data)
                .map(value -> (value - min) / denom)
                .toArray();
    }
}
