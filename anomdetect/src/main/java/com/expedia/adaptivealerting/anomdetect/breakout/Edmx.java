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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isTrue;

/**
 * Breakout detector (a.k.a. change point detector) based on the E-Divisive with Exact Medians (EDM-X) algorithm as
 * described in "Leveraging Cloud Data to Mitigate User Experience from Breaking Bad", by James et al. See
 * https://arxiv.org/abs/1411.7955 for details.
 */
@UtilityClass
@Slf4j
public class Edmx {
    private static final BreakoutResult NO_BREAKOUT_RESULT = new BreakoutResult(-1, 0.0);
    private static final double SIGNIFICANCE_LEVEL = 0.05;

    /**
     * Runs EDM-X on the given time series data. This method scales the data to [0, 1] per the Appendix.
     *
     * @param data  Time series data
     * @param delta Minimum sample size for computing a median. This applies to both the left and the right medians.
     * @return EDM-X breakout result
     */
    public static BreakoutResult edmx(List<Double> data, int delta, int numPerms) {
        val scaledData = unitScale(data);
        val result = estimateLocation(scaledData, delta);
        val significant = isSignificant(scaledData, delta, numPerms, result.getStat());
        return significant ? result : NO_BREAKOUT_RESULT;
    }

    /**
     * Scales the data to the interval [0, 1]. See the appendix in the paper.
     *
     * @param data unscaled data
     * @return data scaled to [0, 1]
     */
    private static List<Double> unitScale(List<Double> data) {
        val summaryStats = data.stream()
                .mapToDouble(Double::doubleValue)
                .summaryStatistics();
        val min = summaryStats.getMin();
        val max = summaryStats.getMax();
        val range = max - min;
        val denom = (range == 0.0 ? 1.0 : range);
        return data.stream()
                .map(value -> (value - min) / denom)
                .collect(Collectors.toList());
    }

    private static BreakoutResult estimateLocation(List<Double> data, int delta) {
        isTrue(data.size() >= 2 * delta, "Required: data.size >= 2 * delta");

        val n = data.size();
        double bestStat = Double.MIN_VALUE;
        int bestLoc = -1;

        val heapsL = new RunningMedian();
        for (int i = 0; i < delta - 1; i++) {
            heapsL.add(data.get(i));
        }
        for (int i = delta; i < n - delta + 1; i++) {
            heapsL.add(data.get(i - 1));
            val mL = heapsL.getMedian();

            val heapsR = new RunningMedian();
            for (int j = i; j < i + delta - 1; j++) {
                heapsR.add(data.get(j));
            }
            for (int j = i + delta; j < n + 1; j++) {
                heapsR.add(data.get(j - 1));

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
     * Performs a significance test of a proposed breakout, represented by the breakout's test statistic.
     *
     * @param data     data series
     * @param delta    minimum segment width
     * @param numPerms number of permutations to use in the permutation test
     * @param testStat test statistic used to estimate the p-value via the permutation test
     * @return boolean indicating whether the test stat is statistically significant
     */
    private static boolean isSignificant(List<Double> data, int delta, int numPerms, double testStat) {
        val perm = new ArrayList<>(data);
        int numPermsGte = 0;

        for (int i = 0; i < numPerms; i++) {
            Collections.shuffle(perm);
            val result = estimateLocation(perm, delta);
//            log.trace("stat={}, perm={}", result.getStat(), perm);
            if (result.getStat() >= testStat) {
                numPermsGte++;
            }
        }

        val estPValue = (double) numPermsGte / (double) (numPerms + 1);
        log.trace("estimated p-value: {}", estPValue);
        return estPValue <= SIGNIFICANCE_LEVEL;
    }
}
