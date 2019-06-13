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
package com.expedia.adaptivealerting.anomdetect.breakout;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Arrays;

/**
 * Breakout detector (a.k.a. change point detector) based on the E-Divisive with Exact Medians (EDM-X) algorithm as
 * described in "Leveraging Cloud Data to Mitigate User Experience from Breaking Bad", by James et al. See
 * https://arxiv.org/abs/1411.7955 for details.
 */
@UtilityClass
@Slf4j
public class Edmx {

    /**
     * Runs EDM-X on the given time series data. This method scales the data to [0, 1] per the Appendix.
     *
     * @param data  Time series data
     * @param delta Minimum sample size for computing a median. This applies to both the left and the right medians.
     * @return EDM-X breakout result
     */
    public static BreakoutResult edmx(double[] data, int delta) {
        val scaledData = unitScale(data);
        val n = scaledData.length;

        double bestStat = Double.MIN_VALUE;
        int bestLoc = -1;

        val heapsL = new RunningMedian();
        for (int i = 0; i < delta - 1; i++) {
            heapsL.add(scaledData[i]);
        }
        for (int i = delta; i < n - delta + 1; i++) {
            heapsL.add(scaledData[i - 1]);
            val mL = heapsL.getMedian();

            val heapsR = new RunningMedian();
            for (int j = i; j < i + delta - 1; j++) {
                heapsR.add(scaledData[j]);
            }
            for (int j = i + delta; j < n + 1; j++) {
                heapsR.add(scaledData[j - 1]);

                val mR = heapsR.getMedian();
                val diff = mL - mR;

                // Order matters here, as we want to avoid integer division
                val stat = diff * diff * i * (j - i) / j;

                if (stat > bestStat) {
                    bestLoc = i;
                    bestStat = stat;
                }
            }
        }

        return new BreakoutResult(bestLoc, bestStat);
    }

    /**
     * Scales the data to the interval [0, 1]. See the appendix in the paper.
     *
     * @param data unscaled data
     * @return data scaled to [0, 1]
     */
    private static double[] unitScale(double[] data) {
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
