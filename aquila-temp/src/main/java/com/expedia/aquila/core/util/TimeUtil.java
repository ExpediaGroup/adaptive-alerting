/*
 * Copyright 2018 Expedia Group, Inc.
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
package com.expedia.aquila.core.util;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Date- and time-related utility functions, isolated here to avoid cluttering up the main code.
 *
 * @author Willie Wheeler
 * @author Karan Shah
 */
public final class TimeUtil {
    private static final int DAYS_PER_WEEK = 7;
    private static final int MILLIS_PER_MINUTE = 60 * 1000;
    
    /**
     * Prevent instantiation.
     */
    private TimeUtil() {
    }
    
    public static int calculateTickOffsetFromSundayMidnightFloor(Instant instant, int tickSizeInMinutes) {
        notNull(instant, "instant can't be null");
        final Instant baseInstant = findSundayMidnightFloor(instant);
        final int offsetInMinutes = (int) (instant.toEpochMilli() - baseInstant.toEpochMilli()) / MILLIS_PER_MINUTE;
        return offsetInMinutes / tickSizeInMinutes;
    }
    
    private static Instant findSundayMidnightFloor(Instant instant) {
        final Instant midnight = instant.truncatedTo(ChronoUnit.DAYS);
        final int dow = ZonedDateTime.ofInstant(midnight, ZoneOffset.UTC).getDayOfWeek().getValue() % DAYS_PER_WEEK;
        return midnight.minus(Duration.ofDays(dow));
    }
}
