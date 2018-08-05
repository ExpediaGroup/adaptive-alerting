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
package com.expedia.adaptivealerting.core.data.io;

import com.expedia.adaptivealerting.core.data.Metric;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * @author Willie Wheeler
 */
public final class MetricFileResolver {
    
    /**
     * Resolves the given metric to a path, returning {@literal null} if there's no mapped path.
     *
     * @param metric Metric.
     * @return Metric path, or {@literal null}.
     */
    public MetricFileInfo resolve(Metric metric) {
        notNull(metric, "metric can't be null");
        
        // FIXME This is obviously a temporary, hardcoded implementation.
        // We'll replace it with the real thing as we onboard more metrics. [WLW]
        if ("count".equals(metric.getTag("mtype")) &&
                "".equals(metric.getTag("unit")) &&
                "bookings".equals(metric.getTag("what")) &&
                "hotels".equals(metric.getTag("lob")) &&
                "expedia-com".equals(metric.getTag("pos")) &&
                "5m".equals(metric.getTag("interval"))) {
            
            return resolveExpBookingsMetric(metric);
        }
        
        throw new MetricNotFoundException(metric);
    }
    
    private MetricFileInfo resolveExpBookingsMetric(Metric metric) {
        
        // TODO We need the POS in here too.
        final String path = metric.getTag("what") + "/" + metric.getTag("lob");
        final MetricFileLocation location = new MetricFileLocation(path, "yyyy-MM-dd", "txt");
        final MetricFileFormat format = new MetricFileFormat(false, false, 5);
        return new MetricFileInfo(location, format);
    }
}
