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
package com.expedia.adaptivealerting.modelservice.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");

    public static Date toUTCDate(String dateStr) {
        try {
            return buildDateFormat().parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static Date now() {
        TimeZone.setDefault(UTC_TIME_ZONE);
        return new Date();
    }

    public static String toUtcDateString(Instant instant) {
        return buildDateFormat().format(Date.from(instant));
    }

    public static String instantToDate(Instant instant) {
        Date date = new Date(instant.getEpochSecond() * 1000);
        return buildDateFormat().format(date);
    }

    private static DateFormat buildDateFormat() {
        DateFormat format = new SimpleDateFormat(DATE_FORMAT);
        format.setTimeZone(UTC_TIME_ZONE);
        return format;
    }
}
