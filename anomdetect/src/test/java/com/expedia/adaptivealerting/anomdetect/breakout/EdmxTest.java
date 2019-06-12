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

import com.opencsv.CSVReaderBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Random;

import static org.junit.Assert.assertEquals;

@Slf4j
public class EdmxTest {
    private static final double TOLERANCE = 0.001;

    @Test
    public void testEdmx_whiteNoiseWithBreakout1() throws Exception {
        val is = ClassLoader.getSystemResourceAsStream("datasets/white-noise-with-breakout-1.csv");
        val isr = new BufferedReader(new InputStreamReader(is));
        val rows = new CSVReaderBuilder(isr).build().readAll();
        val data = rows.stream()
                .mapToDouble(row -> Double.parseDouble(row[0]))
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
