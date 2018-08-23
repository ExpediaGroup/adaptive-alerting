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
package com.expedia.adaptivealerting.dataservice;

import com.expedia.adaptivealerting.core.data.MetricDefinition;
import com.expedia.adaptivealerting.core.data.MetricFrame;
import com.expedia.adaptivealerting.core.data.io.MetricFileFormat;
import com.expedia.adaptivealerting.core.data.io.MetricFileInfo;
import com.expedia.adaptivealerting.core.data.io.MetricFileResolver;
import com.expedia.adaptivealerting.core.data.io.MetricFrameLoader;
import com.expedia.adaptivealerting.core.util.DailyDateRangeDecomposer;
import com.expedia.adaptivealerting.core.util.DateRangeDecomposer;
import com.expedia.adaptivealerting.core.util.MetricUtil;
import com.expedia.adaptivealerting.core.util.ReflectionUtil;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.stream.Collectors;

import static com.expedia.adaptivealerting.core.util.AssertUtil.isTrue;
import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Abstract base class for implementing data services.
 *
 * @author Willie Wheeler
 */
@Slf4j
public abstract class AbstractDataService implements DataService {
    private static final String DATE_RANGE_DECOMPOSER_CLASS_KEY = "dateRangeDecomposer.class";
    
    private DateRangeDecomposer dateRangeDecomposer = new DailyDateRangeDecomposer();
    private MetricFileResolver metricFileResolver = new MetricFileResolver();
    
    public DateRangeDecomposer getDateRangeDecomposer() {
        return dateRangeDecomposer;
    }
    
    public MetricFileResolver getMetricFileResolver() {
        return metricFileResolver;
    }
    
    /**
     * Implementations should call <code>super.init(Config)</code>.
     *
     * @param config Data service configuration.
     */
    @Override
    public void init(Config config) {
        notNull(config, "config can't be null");
        initDateRangeDecomposer(config);
    }
    
    @Override
    public MetricFrame getMetricFrame(MetricDefinition metric, Instant startDate, Instant endDate) {
        notNull(metric, "metric can't be null");
        notNull(startDate, "startDate can't be null");
        notNull(endDate, "endDate can't be null");
        isTrue(!startDate.isAfter(endDate), "startDate cannot be after endDate");
        
        return MetricUtil.merge(dateRangeDecomposer.decompose(startDate, endDate)
                .stream()
                .map(date -> doGetMetricFrame(metric, date))
                .collect(Collectors.toList()));
    }
    
    protected abstract InputStream toInputStream(MetricFileInfo meta, Instant date) throws IOException;
    
    private void initDateRangeDecomposer(Config config) {
        if (config.hasPath(DATE_RANGE_DECOMPOSER_CLASS_KEY)) {
            final String className = config.getString(DATE_RANGE_DECOMPOSER_CLASS_KEY);
            this.dateRangeDecomposer = (DateRangeDecomposer) ReflectionUtil.newInstance(className);
        }
    }
    
    private MetricFrame doGetMetricFrame(MetricDefinition metric, Instant date) {
        final MetricFileInfo meta = getMetricFileResolver().resolve(metric);
        final MetricFileFormat format = meta.getFormat();
        final String path = meta.getLocation().toMetricFilePath(date);
        log.trace("Getting MetricFrame: path={}", path);
        
        final boolean hasHeader = format.getHasHeader();
        final boolean hasTimestamps = format.getHasTimestamps();
        
        try (final InputStream in = toInputStream(meta, date)) {
            if (hasTimestamps) {
                return MetricFrameLoader.loadCsv(metric, in, hasHeader);
            } else {
                final Integer intervalInMinutes = format.getIntervalInMinutes();
                return MetricFrameLoader.loadCsvMissingTimestamps(metric, in, hasHeader, date, intervalInMinutes);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
