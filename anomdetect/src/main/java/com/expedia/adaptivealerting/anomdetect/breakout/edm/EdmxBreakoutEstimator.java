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
package com.expedia.adaptivealerting.anomdetect.breakout.edm;

import com.expedia.adaptivealerting.anomdetect.util.ArrayUtil;
import com.expedia.adaptivealerting.anomdetect.util.math.DataUtil;
import com.expedia.adaptivealerting.anomdetect.util.math.RunningMedian;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.Arrays;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isTrue;

@UtilityClass
public class EdmxBreakoutEstimator {

    /**
     * Runs EDM-X on the given time series data. This method scales the data to [0, 1] per the Appendix.
     *
     * @param data     Time series data
     * @param delta    Minimum sample size for computing a median. This applies to both the left and the right medians.
     * @param numPerms Number of permutations to create for the permutation test
     * @param alpha    Significance level for the alpha test
     * @return EDM-X breakout result
     */
    public static EdmxBreakoutEstimate estimate(double[] data, int delta, int numPerms, double alpha) {
        val scaledData = DataUtil.unitScale(data);
        val breakout = estimateSimpleBreakout(scaledData, delta);
        val energyDistance = breakout.getEnergyDistance();
        val pValue = estimatePValue(scaledData, delta, numPerms, energyDistance);
        val significant = pValue <= alpha;
        return new EdmxBreakoutEstimate(breakout.getLocation(), energyDistance, pValue, alpha, significant);
    }

    private static SimpleEdmxBreakoutEstimate estimateSimpleBreakout(double[] data, int delta) {
        isTrue(data.length >= 2 * delta, "Required: data.size >= 2 * delta");

        val n = data.length;
        int bestLoc = -1;
        double bestStat = Double.MIN_VALUE;

        val heapsL = new RunningMedian();
        for (int i = 0; i < delta - 1; i++) {
            heapsL.add(data[i]);
        }
        for (int i = delta; i < n - delta + 1; i++) {
            heapsL.add(data[i - 1]);
            val mL = heapsL.getMedian();

            val heapsR = new RunningMedian();
            for (int j = i; j < i + delta - 1; j++) {
                heapsR.add(data[j]);
            }
            for (int j = i + delta; j < n + 1; j++) {
                heapsR.add(data[j - 1]);

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

        return new SimpleEdmxBreakoutEstimate(bestLoc, bestStat);
    }

    /**
     * Estimates energy distance p-values using a procedure described in "Leveraging Cloud Data to Mitigate User
     * Experience from 'Breaking Bad'" by James, et al: https://arxiv.org/abs/1411.7955
     */
    private static double estimatePValue(double[] data, int delta, int numPerms, Double testStat) {
        val perm = Arrays.copyOf(data, data.length);

        // # permutations with energy distances greater than the test statistic.
        int numGreater = 0;

        for (int i = 0; i < numPerms; i++) {
            ArrayUtil.shuffle(perm);
            val permEstimate = estimateSimpleBreakout(perm, delta);
            if (permEstimate.getEnergyDistance() >= testStat) {
                numGreater++;
            }
        }

        // Use 1.0 instead of 1 to force conversion to double.
        return numGreater / (numPerms + 1.0);
    }

    @Data
    @AllArgsConstructor
    public static class SimpleEdmxBreakoutEstimate {

        /**
         * Estimated breakout location.
         */
        private int location;

        /**
         * Estimated energy distance between the pre- and post-breakout samples. This is a divergence measure.
         */
        private double energyDistance;
    }
}
