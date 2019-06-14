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
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

@Slf4j
public class EdmxTest {
    private static final double TOLERANCE = 0.001;

    @Test
    public void testEdmx_range0() {
        val data = new double[100];
        for (int i = 0; i < data.length; i++) {
            data[i] = 1.0;
        }
        val result = Edmx.edmx(data, 24);
        log.trace("result={}", result);

        // TODO Might want to make this series throw an exception since the R package fails on division by 0.
        assertEquals(-1, result.getLocation());
    }

    @Test
    public void testEdmx_noBreakout() {
        val data = new double[100];
        for (int i = 0; i < data.length; i += 2) {
            data[i] = 1.0;
            data[i + 1] = 1.2;
        }
        val result = Edmx.edmx(data, 5);
        log.trace("result={}", result);

        // This is the correct behavior when there's no permutation test.
        // TODO Implement permutation test so we can avoid reporting a breakout where none exists.
        assertEquals(49, result.getLocation());
        assertEquals(24.99, result.getStat(), TOLERANCE);
    }

    @Test
    public void testEdmx_whiteNoiseWithBreakout1() throws Exception {
        val metricDef = TestObjectMother.metricDefinition();
        val is = ClassLoader.getSystemResourceAsStream("datasets/white-noise-with-breakout-at-row-600.csv");
        val metricFrame = MetricFrameLoader.loadCsv(metricDef, is, false);
        val metricDataList = metricFrame.getMetricData();
        val data = metricDataList.stream()
                .mapToDouble(metricData -> metricData.getValue())
                .toArray();

        val result = Edmx.edmx(data, 24);
        log.trace("result={}", result);

        // These are the values that Twitter's BreakoutDetection R package produces.
        assertEquals(600, result.getLocation());
        assertEquals(18.02054, result.getStat(), TOLERANCE);
    }

    @Test
    public void testEdmx() {
        val random = new Random();
        val data = new double[100];
        for (int i = 0; i < data.length; i++) {
            data[i] = random.nextGaussian();
        }
        for (int i = 80; i < data.length; i++) {
//            data[i] += 100.0;
            data[i] += 3.0;
        }

        val result = Edmx.edmx(data, 10);
        log.info("result={}", result);
    }

    @Test
    public void testEdmx2() {
        val random = new Random();
        val data = new double[10];
        for (int i = 0; i < data.length; i++) {
            data[i] = random.nextGaussian();
        }
        for (int i = 8; i < data.length; i++) {
//            data[i] += 100.0;
            data[i] += 3.0;
        }

        val result = Edmx.edmx(data, 2);
        log.info("result={}", result);
    }

    @Test
    public void testEdmx3() {
        val random = new Random();
        val data = new double[10];
        for (int i = 0; i < data.length; i++) {
            data[i] = random.nextGaussian();
        }
        for (int i = 8; i < data.length; i++) {
//            data[i] += 100.0;
            data[i] += 100.0;
        }

        val result = Edmx.edmx(data, 2);
        log.info("result={}", result);
    }
}
