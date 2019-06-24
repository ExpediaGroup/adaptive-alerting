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

import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class ArrayUtil {

    /**
     * Shuffles the given array in-place, using the Durstenfeld version of the Fisher-Yates shuffle. Implemented this
     * because we want to avoid the overhead of creating wrapper arrays and wrapper Doubles in tight loops such as EDM
     * breakout detection.
     * <p>
     * See
     *
     * <ul>
     * <li>https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle#The_modern_algorithm</li>
     * <li>https://stackoverflow.com/questions/1519736/random-shuffling-of-an-array</li>
     * </ul>
     *
     * @param arr array to shuffle
     */
    public static void shuffle(double[] arr) {
        val rnd = ThreadLocalRandom.current();
        for (int i = arr.length - 1; i > 0; i--) {
            val index = rnd.nextInt(i + 1);
            val elemToSwap = arr[index];
            arr[index] = arr[i];
            arr[i] = elemToSwap;
        }
    }
}
