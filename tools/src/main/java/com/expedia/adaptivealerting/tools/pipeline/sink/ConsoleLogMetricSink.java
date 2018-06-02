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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;
import static com.expedia.adaptivealerting.core.util.MetricPointUtil.outlierLevel;

/**
 * Logs generated data to the console.
 *
 * @author Willie Wheeler
 */
public class ConsoleLogMetricSink implements MetricSink {
    private static final Logger log = LoggerFactory.getLogger(ConsoleLogMetricSink.class);
    
    @Override
    public void next(MetricPoint metricPoint) {
        notNull(metricPoint, "metricPoint can't be null");
        log.info("MetricPoint: name={}, value={}, outlierLevel={}",
                metricPoint.metric(),
                metricPoint.value(),
                outlierLevel(metricPoint));
    }
}
