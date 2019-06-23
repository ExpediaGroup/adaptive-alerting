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

import com.expedia.adaptivealerting.anomdetect.util.MetricFrameLoader;
import com.expedia.adaptivealerting.anomdetect.util.TestObjectMother;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Slf4j
public class EdmxBreakoutEstimatorTest {
    private static final double TOLERANCE = 0.001;
    private static final long RANDOM_SEED = 314159;

    /**
     * Significance level for breakout alpha tests.
     */
    private static final double ALPHA = 0.05;

    private Random random;

    @Before
    public void setUp() {
        this.random = new Random();
        random.setSeed(RANDOM_SEED);
    }

    @Test
    public void testEdmx_range0() {
        val data = new double[100];
        Arrays.fill(data, 1.0);
        val estBreakout = EdmxBreakoutEstimator.estimate(data, 24, 0, ALPHA);

        // TODO Might want to make this series throw an exception since the R package fails on division by 0.
        assertEquals(-1, estBreakout.getLocation());
    }

    @Test
    public void testEdmx_noBreakout() {
        val data = new double[100];
        for (int i = 0; i < data.length; i++) {
            data[i] = i % 2 == 0 ? 1.0 : 1.2;
        }

        // When there's no permutation test, the algo returns the location with the highest energyDistance.
        // Or one of them, I guess, when there are multiple such locations (haven't checked).
        val result1 = EdmxBreakoutEstimator.estimate(data, 5, 0, ALPHA);
        log.trace("result1={}", result1);
        assertEquals(49, result1.getLocation());
        assertEquals(24.99, result1.getEnergyDistance(), TOLERANCE);

        // With permutations
        val result2 = EdmxBreakoutEstimator.estimate(data, 5, 20, ALPHA);
        log.trace("result2={}", result2);
        assertEquals(49, result2.getLocation());
        assertEquals(24.99, result2.getEnergyDistance(), TOLERANCE);
        assertFalse(result2.isSignificant());
    }

    @Test
    public void testEdmx_whiteNoise_breakout() throws Exception {
        val metricDef = TestObjectMother.metricDefinition();
        val is = ClassLoader.getSystemResourceAsStream("datasets/white-noise-with-breakout-at-row-600.csv");
        val metricFrame = MetricFrameLoader.loadCsv(metricDef, is, false);
        val metricDataList = metricFrame.getMetricData();
        val fullData = metricDataList.stream()
                .mapToDouble(metricData -> metricData.getValue())
                .toArray();

        // Various tests. Locations and energy distances come from the R implementation.
        testEdmx_whiteNoise_breakout(fullData, 0, 1000, 24, 600, 18.02054);
        testEdmx_whiteNoise_breakout(fullData, 579, 620, 20, 20, 1.580082);
        testEdmx_whiteNoise_breakout(fullData, 589, 610, 10, 11, 1.089424);
        testEdmx_whiteNoise_breakout(fullData, 589, 610, 5, 11, 1.089424);
        testEdmx_whiteNoise_breakout(fullData, 589, 605, 5, 10, 0.8967453);
    }

    @Test
    public void testEdmx_shortSeries_smallBreakout_noPerms() {
        val data = new double[10];
        for (int i = 0; i < data.length; i++) {
            data[i] = random.nextGaussian() + (i >= 8 ? 3.0 : 0.0);
        }

        // No permutations, small breakout
        val result = EdmxBreakoutEstimator.estimate(data, 2, 0, ALPHA);
        assertEquals(8, result.getLocation());
        assertEquals(0.455, result.getEnergyDistance(), TOLERANCE);
    }

    @Test
    public void testEdmx_shortSeries_largeBreakout_noPerms() {
        val data = new double[10];
        for (int i = 0; i < data.length; i++) {
            data[i] = random.nextGaussian() + (i >= 8 ? 100.0 : 0.0);
        }

        // No permutations, large breakout
        val result = EdmxBreakoutEstimator.estimate(data, 2, 0, ALPHA);
        assertEquals(7, result.getLocation());
        assertEquals(1.986, result.getEnergyDistance(), TOLERANCE);
    }

    @Test
    public void testEdmx_longSeries_smallBreakout_noPerms() {
        val data = new double[100];
        for (int i = 0; i < data.length; i++) {
            data[i] = random.nextGaussian() + (i >= 80 ? 3.0 : 0.0);
        }

        // No permutations, small breakout
        val result = EdmxBreakoutEstimator.estimate(data, 10, 0, ALPHA);
        assertEquals(75, result.getLocation());
        assertEquals(2.569, result.getEnergyDistance(), TOLERANCE);
    }

    private void testEdmx_whiteNoise_breakout(double[] fullData, int start, int end, int delta, int loc, double stat) {
        val data = Arrays.copyOfRange(fullData, start, end);

        // No permutations
        val resultNoPerms = EdmxBreakoutEstimator.estimate(data, delta, 0, ALPHA);
        assertEquals(loc, resultNoPerms.getLocation());
        assertEquals(stat, resultNoPerms.getEnergyDistance(), TOLERANCE);

        // With permutations
        val resultPerms = EdmxBreakoutEstimator.estimate(data, delta, 20, ALPHA);
        assertEquals(loc, resultPerms.getLocation());
        assertEquals(stat, resultPerms.getEnergyDistance(), TOLERANCE);
        assertTrue(resultPerms.isSignificant());
    }
}
