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

import com.expedia.adaptivealerting.tools.pipeline.MetricPointSubscriber;
import com.expedia.www.haystack.commons.entities.MetricPoint;

/**
 * Metric source interface.
 *
 * @author Willie Wheeler
 */
public interface MetricSource {
    
    /**
     * Returns the metric name.
     *
     * @return Metric name.
     */
    String getMetricName();
    
    /**
     * Adds a metric point subscriber to this source.
     *
     * @param subscriber Metric point subscriber.
     */
    void addSubscriber(MetricPointSubscriber subscriber);
    
    /**
     * Removes a metric point subscriber from this source.
     *
     * @param subscriber Metric point subscriber.
     */
    void removeSubscriber(MetricPointSubscriber subscriber);
    
    /**
     * Starts the metric source.
     */
    void start();
    
    /**
     * Returns the next message in the stream.
     *
     * @return Next metric point.
     */
    MetricPoint next();
    
    /**
     * Stops the metric source.
     */
    void stop();
}
