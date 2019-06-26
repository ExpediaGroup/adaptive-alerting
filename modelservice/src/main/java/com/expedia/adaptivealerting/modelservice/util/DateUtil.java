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

import lombok.experimental.UtilityClass;
import lombok.val;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

@UtilityClass
public class DateUtil {
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");

    public static Date toUtcDate(String dateStr) {
        notNull(dateStr, "dateStr can't be null");
        try {
            return buildDateFormat().parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toDateString(Instant instant) {
        notNull(instant, "instant can't be null");
        val date = new Date(instant.getEpochSecond() * 1000);
        return buildDateFormat().format(date);
    }

    public static String toUtcDateString(Instant instant) {
        notNull(instant, "instant can't be null");
        return buildDateFormat().format(Date.from(instant));
    }

    public static Date now() {
        TimeZone.setDefault(UTC_TIME_ZONE);
        return new Date();
    }

    private static DateFormat buildDateFormat() {
        val format = new SimpleDateFormat(DATE_FORMAT);
        format.setTimeZone(UTC_TIME_ZONE);
        return format;
    }
}
