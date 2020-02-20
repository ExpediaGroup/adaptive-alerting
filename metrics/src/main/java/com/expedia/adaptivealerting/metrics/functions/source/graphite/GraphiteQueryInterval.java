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
package com.expedia.adaptivealerting.metrics.functions.source.graphite;

import lombok.Data;
import lombok.val;

@Data
public class GraphiteQueryInterval {

    private long from;
    private long until;

    public GraphiteQueryInterval(long date, long intervalInSeconds) {
        /* using absolute time instead of relative */
        val startOfCurrentInterval = (date / intervalInSeconds) * intervalInSeconds;
        /* Ensuring query is run for the previous bucket of interval.
           This is to improve accuracy of data returned when query is run.
         */
        this.from = startOfCurrentInterval - (1 + intervalInSeconds);
        this.until = this.from + intervalInSeconds;
    }
}