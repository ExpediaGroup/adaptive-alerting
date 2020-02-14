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

import lombok.val;
import org.junit.Test;

import java.time.Instant;

import static java.time.Instant.ofEpochSecond;
import static org.junit.Assert.assertEquals;

public final class DateUtilTest {

    @Test
    public void testTruncatedToDay() {

        // Sunday, 2018-04-01
        doTestTruncatedToDay("2018-04-01T00:00:00Z", "2018-04-01T00:00:00Z");
        doTestTruncatedToDay("2018-04-01T01:00:00Z", "2018-04-01T00:00:00Z");
        doTestTruncatedToDay("2018-04-01T01:30:12Z", "2018-04-01T00:00:00Z");
        doTestTruncatedToDay("2018-04-01T23:59:59Z", "2018-04-01T00:00:00Z");

        // Monday, 2018-04-02
        doTestTruncatedToDay("2018-04-02T00:00:00Z", "2018-04-02T00:00:00Z");
        doTestTruncatedToDay("2018-04-02T12:00:00Z", "2018-04-02T00:00:00Z");
    }

    @Test
    public void testTruncatedToWeek() {

        // Sunday, 2018-04-01
        doTestTruncatedToWeek("2018-04-01T00:00:00Z", "2018-04-01T00:00:00Z");
        doTestTruncatedToWeek("2018-04-01T01:00:00Z", "2018-04-01T00:00:00Z");
        doTestTruncatedToWeek("2018-04-01T01:30:12Z", "2018-04-01T00:00:00Z");
        doTestTruncatedToWeek("2018-04-01T11:59:59Z", "2018-04-01T00:00:00Z");
        doTestTruncatedToWeek("2018-04-02T00:00:00Z", "2018-04-01T00:00:00Z");
        doTestTruncatedToWeek("2018-04-03T01:00:00Z", "2018-04-01T00:00:00Z");
        doTestTruncatedToWeek("2018-04-07T23:59:59Z", "2018-04-01T00:00:00Z");

        // Sunday, 2018-04-08
        doTestTruncatedToWeek("2018-04-08T00:00:00Z", "2018-04-08T00:00:00Z");
        doTestTruncatedToWeek("2018-04-09T00:00:00Z", "2018-04-08T00:00:00Z");
    }

    @Test
    public void testSnapToSeconds() {
        doTestSnappedToSeconds("2018-04-01T01:09:55Z", "2018-04-01T01:05:00Z", 300);
        doTestSnappedToSeconds("2018-04-01T01:07:55Z", "2018-04-01T01:07:00Z", 60);
        doTestSnappedToSeconds("2018-04-01T01:07:55Z", "2018-04-01T01:07:30Z", 30);
    }

    @Test
    public void testTruncatedToSeconds() {
        doTestTruncatedToSeconds("2018-04-01T09:24:54.63Z", "2018-04-01T09:24:54.00Z");
        doTestTruncatedToSeconds("2018-04-01T09:24:54.01Z", "2018-04-01T09:24:54.00Z");
        doTestTruncatedToSeconds("2018-04-01T09:24:53.41Z", "2018-04-01T09:24:53.00Z");
    }

    private void doTestTruncatedToDay(String dateStr, String expectedStr) {
        final Instant date = Instant.parse(dateStr);
        final Instant day = Instant.parse(expectedStr);
        final Instant actual = DateUtil.truncatedToDay(date);
        assertEquals(day, actual);
    }

    private void doTestTruncatedToWeek(String dateStr, String expectedStr) {
        final Instant date = Instant.parse(dateStr);
        final Instant day = Instant.parse(expectedStr);
        final Instant actual = DateUtil.truncatedToWeek(date);
        assertEquals(day, actual);
    }

    private void doTestSnappedToSeconds(String dateStr, String expectedStr, int seconds) {
        final Instant expected = Instant.parse(expectedStr);
        final Instant date = Instant.parse(dateStr);
        final Instant actual = DateUtil.snapToSeconds(date, seconds);
        assertEquals(expected, actual);
    }

    private void doTestTruncatedToSeconds(String dateStr, String expectedStr) {
        final Instant date = Instant.parse(dateStr);
        final Instant day = Instant.parse(expectedStr);
        final Instant actual = DateUtil.truncatedToSeconds(date);
        assertEquals(day, actual);
    }
}
