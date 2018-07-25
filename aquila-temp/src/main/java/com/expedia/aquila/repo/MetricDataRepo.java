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
package com.expedia.aquila.repo;

import com.expedia.adaptivealerting.core.data.Metric;
import com.expedia.adaptivealerting.core.data.MetricFrame;
import com.typesafe.config.Config;

/**
 * Interface for metric data repositories. This is generally intended to support anomaly detector model training.
 *
 * @author Willie Wheeler
 * @author Karan Shah
 */
public interface MetricDataRepo {
    
    /**
     * Configure the repository.
     *
     * @param config Repository configuration.
     */
    void init(Config config);
    
    /**
     * Load repository data.
     *
     * @param metric Metric.
     * @param path   Data path.
     * @return Data.
     */
    MetricFrame load(Metric metric, String path);
}
