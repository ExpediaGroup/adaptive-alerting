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

import com.expedia.adaptivealerting.core.data.MetricFrame;
import com.expedia.metrics.MetricData;

import java.util.ArrayList;
import java.util.List;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Metric utilities.
 *
 * @author Willie Wheeler
 */
public final class MetricUtil {
    
    /**
     * Prevent instantiation.
     */
    private MetricUtil() {
    }
    
    public static MetricFrame merge(List<MetricFrame> frames) {
        notNull(frames, "frames can't be null");
        
        int totalSize = 0;
        for (final MetricFrame frame : frames) {
            totalSize += frame.getNumRows();
        }
        
        final List<MetricData> resultList = new ArrayList<>(totalSize);
        for (final MetricFrame frame : frames) {
            resultList.addAll(frame.getMetricPoints());
        }
        
        return new MetricFrame(resultList);
    }
}
