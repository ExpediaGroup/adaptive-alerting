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
package com.expedia.adaptivealerting.core.metricsource;

import com.expedia.www.haystack.commons.entities.MetricPoint;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Abstract base class for implementing metric sources. Uses a timer to simulate timing behavior.
 *
 * @author Willie Wheeler
 */
public abstract class AbstractMetricSource implements MetricSource {
    private final String name;
    private final long period;
    private final Timer timer = new Timer();
    private final List<MetricSubscriber> subscribers = new LinkedList<>();
    
    /**
     * Creates a new metric source with the given period.
     *
     * @param name   Metric name.
     * @param period Timer period in seconds.
     */
    public AbstractMetricSource(String name, long period) {
        notNull(name, "name can't be null");
        
        this.name = name;
        this.period = period;
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public void addSubscriber(MetricSubscriber subscriber) {
        subscribers.add(subscriber);
    }
    
    @Override
    public void removeSubscriber(MetricSubscriber subscriber) {
        subscribers.remove(subscriber);
    }
    
    @Override
    public void start() {
        timer.scheduleAtFixedRate(new TimerTask() {
            
            @Override
            public void run() {
                publish(next());
            }
        }, 0L, period);
    }
    
    /**
     * Returns the next metric point.
     *
     * @return Next metric point.
     */
    public abstract MetricPoint next();
    
    @Override
    public void stop() {
        timer.cancel();
        timer.purge();
    }
    
    private void publish(MetricPoint metricPoint) {
        subscribers.stream().forEach(listener -> listener.next(metricPoint));
    }
}
