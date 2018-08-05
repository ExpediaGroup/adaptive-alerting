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
package com.expedia.adaptivealerting.core.util;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static com.expedia.adaptivealerting.core.util.AssertUtil.isTrue;
import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Strategy for decomposing a date range into a list of dates. This one returns all weeks in the range.
 *
 * @author Willie Wheeler
 */
public class WeeklyDateRangeDecomposer implements DateRangeDecomposer {
    
    @Override
    public List<Instant> decompose(Instant startDate, Instant endDate) {
        notNull(startDate, "startDate can't be null");
        notNull(endDate, "endDate can't be null");
        isTrue(!startDate.isAfter(endDate), "startDate cannot be after endDate");
    
        final Instant inclLoBd = DateUtil.truncatedToWeek(startDate);
        final Instant exclUpBd = DateUtil.truncatedToWeek(endDate);
    
        final List<Instant> result = new ArrayList<>();
        for (Instant curr = inclLoBd; curr.isBefore(exclUpBd); curr = curr.plus(7, ChronoUnit.DAYS)) {
            result.add(curr);
        }
    
        return result;
    }
}
