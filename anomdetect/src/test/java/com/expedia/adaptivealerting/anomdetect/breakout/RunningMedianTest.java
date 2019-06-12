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

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@Slf4j
public final class RunningMedianTest {
    private static final double TOLERANCE = 0.001;

    private RunningMedian runningMedian;

    @Before
    public void setUp() {
        this.runningMedian = new RunningMedian();
    }

    @Test(expected = RuntimeException.class)
    public void testGetMedian_empty() {
        runningMedian.getMedian();
    }

    @Test
    public void testAddAndGet1() {
        runningMedian.add(-10.0);
        assertEquals(-10.0, runningMedian.getMedian(), TOLERANCE);
        runningMedian.add(-10.0);
        assertEquals(-10.0, runningMedian.getMedian(), TOLERANCE);
        runningMedian.add(-10.0);
        assertEquals(-10.0, runningMedian.getMedian(), TOLERANCE);
        runningMedian.add(-10.0);
        assertEquals(-10.0, runningMedian.getMedian(), TOLERANCE);
    }

    @Test
    public void testAddAndGet2() {
        runningMedian.add(10.0);
        assertEquals(10.0, runningMedian.getMedian(), TOLERANCE);
        runningMedian.add(12.0);
        assertEquals(11.0, runningMedian.getMedian(), TOLERANCE);
        runningMedian.add(8.0);
        assertEquals(10.0, runningMedian.getMedian(), TOLERANCE);
        runningMedian.add(6.0);
        assertEquals(9.0, runningMedian.getMedian(), TOLERANCE);
        runningMedian.add(20.0);
        assertEquals(10.0, runningMedian.getMedian(), TOLERANCE);
        runningMedian.add(20.0);
        assertEquals(11.0, runningMedian.getMedian(), TOLERANCE);
    }

    @Test
    public void testAddAndGet3() {
        // I generated these in Excel by computing medians in the traditional fashion.
        runningMedian.add(1.6122247);
        assertEquals(1.6122247, runningMedian.getMedian(), TOLERANCE);
        runningMedian.add(0.003578898);
        assertEquals(0.807901799, runningMedian.getMedian(), TOLERANCE);
        runningMedian.add(0.071160659);
        assertEquals(0.071160659, runningMedian.getMedian(), TOLERANCE);
        runningMedian.add(1.484135631);
        assertEquals(0.777648145, runningMedian.getMedian(), TOLERANCE);
        runningMedian.add(2.661897476);
        assertEquals(1.484135631, runningMedian.getMedian(), TOLERANCE);
        runningMedian.add(2.994276183);
        assertEquals(1.548180166, runningMedian.getMedian(), TOLERANCE);
        runningMedian.add(1.917423178);
        assertEquals(1.6122247, runningMedian.getMedian(), TOLERANCE);
        runningMedian.add(1.287120095);
        assertEquals(1.548180166, runningMedian.getMedian(), TOLERANCE);
        runningMedian.add(0.718125133);
        assertEquals(1.484135631, runningMedian.getMedian(), TOLERANCE);
        runningMedian.add(1.637677667);
        assertEquals(1.548180166, runningMedian.getMedian(), TOLERANCE);
        runningMedian.add(1.718019171);
        assertEquals(1.6122247, runningMedian.getMedian(), TOLERANCE);
        runningMedian.add(2.75629859);
        assertEquals(1.624951184, runningMedian.getMedian(), TOLERANCE);
        runningMedian.add(2.304211684);
        assertEquals(1.637677667, runningMedian.getMedian(), TOLERANCE);
        runningMedian.add(1.984439829);
        assertEquals(1.677848419, runningMedian.getMedian(), TOLERANCE);
        runningMedian.add(1.365237299);
        assertEquals(1.637677667, runningMedian.getMedian(), TOLERANCE);
        runningMedian.add(1.602881923);
        assertEquals(1.624951184, runningMedian.getMedian(), TOLERANCE);
        runningMedian.add(2.205183371);
        assertEquals(1.637677667, runningMedian.getMedian(), TOLERANCE);
        runningMedian.add(1.622812403);
        assertEquals(1.630245035, runningMedian.getMedian(), TOLERANCE);
        runningMedian.add(1.293948723);
        assertEquals(1.622812403, runningMedian.getMedian(), TOLERANCE);
        runningMedian.add(2.220757498);
        assertEquals(1.630245035, runningMedian.getMedian(), TOLERANCE);
    }
}
