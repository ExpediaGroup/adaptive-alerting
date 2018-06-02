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
package com.expedia.adaptivealerting.tools.pipeline.sink;

import com.expedia.adaptivealerting.tools.pipeline.MetricSink;
import com.expedia.www.haystack.commons.entities.MetricPoint;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

public class ChartSink implements MetricSink {
    private final TimeSeries timeSeries;
    
    // FIXME For now, just impose timestamps.
    // Later update to use actual metric point timestamps.
    private Second currentSecond;
    
    public ChartSink(TimeSeries timeSeries) {
        notNull(timeSeries, "timeSeries can't be null");
        this.timeSeries = timeSeries;
    }
    
    @Override
    public void next(MetricPoint metricPoint) {
        this.currentSecond = (currentSecond == null ? new Second() : (Second) currentSecond.next());
        timeSeries.add(currentSecond, metricPoint.value());
    }
}
