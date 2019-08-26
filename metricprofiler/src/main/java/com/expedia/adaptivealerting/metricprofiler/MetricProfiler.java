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
package com.expedia.adaptivealerting.metricprofiler;

import com.expedia.adaptivealerting.metricprofiler.source.ProfilingSource;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.metrictank.MetricTankIdFactory;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import lombok.var;

import java.util.HashMap;
import java.util.Map;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

/**
 * Metric profiler finds whether metric profiling exists for a given metrics or not.
 * <p>
 * Metric profiler manager maintains an internal cache of (String : Boolean) where key is the metric hash.
 * </p>
 **/
@Slf4j
public class MetricProfiler {

    @Getter
    @NonNull
    private ProfilingSource profilingSource;

    private final Map<String, Boolean> cachedMetrics;
    private final MetricTankIdFactory idFactory = new MetricTankIdFactory();

    /**
     * Creates a new metric profiler manager from the given parameters.
     *
     * @param profilingSource profiling source
     * @param cachedMetrics   map containing cached metric
     */
    public MetricProfiler(ProfilingSource profilingSource, Map<String, Boolean> cachedMetrics) {
        this.profilingSource = profilingSource;
        this.cachedMetrics = cachedMetrics;
    }

    public MetricProfiler(ProfilingSource profilingSource) {
        this(profilingSource, new HashMap<>());
    }

    /**
     * Finds whether profiling exists or not for a given metric.
     *
     * @param metricData Metric data.
     * @return Boolean
     */
    public Boolean hasProfilingInfo(MetricData metricData) {
        notNull(metricData, "metricData can't be null");
        val hash = idFactory.getId(metricData.getMetricDefinition());
        var profilingExists = cachedMetrics.get(hash);
        if (profilingExists == null) {
            val tags = metricData.getMetricDefinition().getTags().getKv();
            profilingExists = profilingSource.profilingExists(tags);
            cachedMetrics.put(hash, profilingExists);
        } else {
            log.trace("Fetched profiling info from cache");
        }
        return profilingExists;
    }
}
