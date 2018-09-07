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
package com.expedia.adaptivealerting.dataconnect;

import com.expedia.adaptivealerting.core.data.MetricFrame;
import com.expedia.adaptivealerting.core.util.ReflectionUtil;
import com.expedia.adaptivealerting.dataservice.DataService;
import com.expedia.adaptivealerting.core.metrics.MetricDefinition;
import com.typesafe.config.Config;

import java.time.Instant;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Connector implementation that does a pass-through call to a backing {@link DataService} bean.
 *
 * @author Willie Wheeler
 */
public final class DirectDataConnector implements DataConnector {
    private DataService dataService;
    
    @Override
    public void init(Config config) {
        notNull(config, "config can't be null");
        
        final Config dataServiceConfig = config.getConfig("dataService");
        notNull(dataServiceConfig, "Property dataService must be defined");
        
        final String dataServiceClassName = dataServiceConfig.getString("class");
        notNull(dataServiceClassName, "Property dataService.class must be defined");
        
        this.dataService = (DataService) ReflectionUtil.newInstance(dataServiceClassName);
        dataService.init(dataServiceConfig);
    }
    
    @Override
    public MetricFrame load(MetricDefinition metric, Instant startDate, Instant endDate) {
        notNull(metric, "metric can't be null");
        notNull(startDate, "startDate can't be null");
        notNull(endDate, "endDate can't be null");
        return dataService.getMetricFrame(metric, startDate, endDate);
    }
}
