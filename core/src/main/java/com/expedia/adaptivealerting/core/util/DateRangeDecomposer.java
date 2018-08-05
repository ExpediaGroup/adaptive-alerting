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
import java.util.List;

/**
 * Interface for different strategies for decomposing a date range into dates. Intended primarily to support loading
 * training data files.
 *
 * @author Willie Wheeler
 */
public interface DateRangeDecomposer {
    
    /**
     * Decompose the given date range into a list of dates.
     *
     * @param startDate Start date. Its floor is the inclusive lower bound for the range.
     * @param endDate   End date. Its floor is the exclusive upper bound for the range.
     * @return Date range decomposition.
     */
    List<Instant> decompose(Instant startDate, Instant endDate);
}
