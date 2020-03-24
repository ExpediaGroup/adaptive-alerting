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
package com.expedia.adaptivealerting.anomdetect.testutil;

import java.time.ZonedDateTime;

import static java.time.ZoneOffset.UTC;

public class DateHelper {
    /**
     * Creates a graphite-compatible timestamp long, i.e. result represents number of seconds since epoch, for given date/time
     */
    public static long timestamp(int year, int month, int dayOfMonth, int hour, int minute, int second) {
        ZonedDateTime dateTime = ZonedDateTime.of(year, month, dayOfMonth, hour, minute, second, 0, UTC);
        return dateTime.toInstant().getEpochSecond();
    }
}
