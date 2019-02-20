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
package com.expedia.adaptivealerting.tools.pipeline.source;

import com.expedia.adaptivealerting.tools.pipeline.util.MetricDataSubscriber;
import com.expedia.metrics.MetricData;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.expedia.adaptivealerting.core.util.AssertUtil.isTrue;
import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Abstract base class for implementing metric sources. Uses a timer to simulate timing behavior.
 */
public abstract class AbstractMetricSource implements MetricSource {
//    private final MetricDefinition metricDefinition;
    private final long periodMs;
    private final List<MetricDataSubscriber> subscribers = new LinkedList<>();
    
    private final Timer timer = new Timer();
    
    /**
     * Creates a new metric source with the given period.
     *
     * @param metricKey Metric key.
     * @param periodMs  Timer period in milliseconds.
     */
    // TODO: Remove unused metricKey parameter
    public AbstractMetricSource(String metricKey, long periodMs) {
        notNull(metricKey, "metricKey can't be null");
        isTrue(periodMs > 0, "periodMs must be > 0");
        
//        this.metricDefinition = new MetricDefinition(metricKey);
        this.periodMs = periodMs;
    }
    
//    @Override
//    public MetricDefinition getMetricDefinition() {
//        return metricDefinition;
//    }
    
    @Override
    public void start() {
        timer.scheduleAtFixedRate(new TimerTask() {
            
            @Override
            public void run() {
                final MetricData next = next();
                if (next != null) {
                    publish(next);
                }
            }
        }, 0L, periodMs);
    }
    
    @Override
    public void stop() {
        timer.cancel();
        timer.purge();
    }
    
    @Override
    public void addSubscriber(MetricDataSubscriber subscriber) {
        notNull(subscriber, "subscriber can't be null");
        subscribers.add(subscriber);
    }
    
    @Override
    public void removeSubscriber(MetricDataSubscriber subscriber) {
        notNull(subscriber, "subscriber can't be null");
        subscribers.remove(subscriber);
    }
    
    private void publish(MetricData metricData) {
        subscribers.stream().forEach(subscriber -> subscriber.next(metricData));
    }
}
