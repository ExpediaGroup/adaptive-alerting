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

import com.expedia.adaptivealerting.anomdetect.util.MetricFrameLoader;
import com.expedia.adaptivealerting.anomdetect.util.TestObjectMother;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@Slf4j
public class EdmxTest {
    private static final double TOLERANCE = 0.001;
    private static final long RANDOM_SEED = 314159;

    private Random random;

    @Before
    public void setUp() {
        this.random = new Random();
        random.setSeed(RANDOM_SEED);
    }

    @Test
    public void testEdmx_range0() {
        val data = new ArrayList<Double>(100);
        for (int i = 0; i < 100; i++) {
            data.add(1.0);
        }
        val result = Edmx.edmx(data, 24, 0);
        log.trace("result={}", result);

        // TODO Might want to make this series throw an exception since the R package fails on division by 0.
        assertEquals(-1, result.getLocation());
    }

    @Test
    public void testEdmx_noBreakout() {
        val data = new ArrayList<Double>(100);
        for (int i = 0; i < 50; i++) {
            data.add(1.0);
            data.add(1.2);
        }

        // When there's no permutation test, the algo returns the location with the highest statistic.
        // Or one of them, I guess, when there are multiple such locations (haven't checked).
        val result = Edmx.edmx(data, 5, 0);
        assertEquals(49, result.getLocation());
        assertEquals(24.99, result.getStat(), TOLERANCE);

        // With permutations
        val result2 = Edmx.edmx(data, 5, 20);
        assertEquals(-1, result2.getLocation());
    }

    @Test
    public void testEdmx_whiteNoise_breakout() throws Exception {
        val metricDef = TestObjectMother.metricDefinition();
        val is = ClassLoader.getSystemResourceAsStream("datasets/white-noise-with-breakout-at-row-600.csv");
        val metricFrame = MetricFrameLoader.loadCsv(metricDef, is, false);
        val metricDataList = metricFrame.getMetricData();
        val data = metricDataList.stream()
                .map(metricData -> metricData.getValue())
                .collect(Collectors.toList());

        // No permutations
        val result = Edmx.edmx(data, 24, 0);
        assertEquals(600, result.getLocation());
        assertEquals(18.02054, result.getStat(), TOLERANCE);

        // With permutations
        val result2 = Edmx.edmx(data, 24, 20);
        assertEquals(600, result2.getLocation());
        assertEquals(18.02054, result2.getStat(), TOLERANCE);
    }

    @Test
    public void testEdmx_longSeries_smallBreakout() {
        val data = new ArrayList<Double>(100);
        for (int i = 0; i < 80; i++) {
            data.add(random.nextGaussian());
        }
        for (int i = 80; i < 100; i++) {
            data.add(random.nextGaussian() + 3.0);
        }

        // No permutations, small breakout
        val result = Edmx.edmx(data, 10, 0);
        assertEquals(75, result.getLocation());
        assertEquals(2.569, result.getStat(), TOLERANCE);
    }

    @Test
    public void testEdmx_shortSeries_smallBreakout() {
        val data = new ArrayList<Double>(10);
        for (int i = 0; i < 8; i++) {
            data.add(random.nextGaussian());
        }
        for (int i = 8; i < 10; i++) {
            data.add(random.nextGaussian() + 3.0);
        }

        // No permutations, small breakout
        val result = Edmx.edmx(data, 2, 0);
        assertEquals(8, result.getLocation());
        assertEquals(0.455, result.getStat(), TOLERANCE);
    }

    @Test
    public void testEdmx_shortSeries_largeBreakout() {
        val data = new ArrayList<Double>(10);
        for (int i = 0; i < 8; i++) {
            data.add(random.nextGaussian());
        }
        for (int i = 8; i < 10; i++) {
            data.add(random.nextGaussian() + 100.0);
        }

        // No permutations, large breakout
        val result = Edmx.edmx(data, 2, 0);
        assertEquals(7, result.getLocation());
        assertEquals(1.986, result.getStat(), TOLERANCE);
    }
}
