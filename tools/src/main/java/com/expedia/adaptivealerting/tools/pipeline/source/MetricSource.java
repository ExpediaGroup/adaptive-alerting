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
package com.expedia.adaptivealerting.tools.pipeline.source;

import com.expedia.adaptivealerting.tools.pipeline.util.MetricDataSubscriber;
import com.expedia.metrics.MetricData;

/**
 * Metric source interface.
 *
 * @author Willie Wheeler
 */
public interface MetricSource {
    
//    /**
//     * Returns the metric definition.
//     *
//     * @return Metric definition.
//     */
//    MetricDefinition getMetricDefinition();
    
    /**
     * Adds a metric point subscriber to this source.
     *
     * @param subscriber Metric point subscriber.
     */
    void addSubscriber(MetricDataSubscriber subscriber);
    
    /**
     * Removes a metric point subscriber from this source.
     *
     * @param subscriber Metric point subscriber.
     */
    void removeSubscriber(MetricDataSubscriber subscriber);
    
    /**
     * Starts the metric source.
     */
    void start();
    
    /**
     * Returns the next message in the stream.
     *
     * @return Next metric data.
     */
    MetricData next();
    
    /**
     * Stops the metric source.
     */
    void stop();
}
