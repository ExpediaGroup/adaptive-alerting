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

import com.expedia.adaptivealerting.core.data.Metric;
import com.expedia.adaptivealerting.core.data.MetricFrame;
import com.typesafe.config.Config;

import java.time.Instant;

/**
 * Interface for metric data connectors. This is generally intended to support anomaly detector model training.
 *
 * @author Willie Wheeler
 * @author Karan Shah
 */
public interface DataConnector {
    
    /**
     * Initializes the connector.
     *
     * @param config Configuration object.
     */
    void init(Config config);
    
    /**
     * Loads a metric frame.
     *
     * @param metric    Metric whose data we want to load.
     * @param startDate Start date.
     * @param endDate   End date.
     * @return Metric frame.
     */
    MetricFrame load(Metric metric, Instant startDate, Instant endDate);
}
