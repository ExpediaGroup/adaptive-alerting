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

import com.expedia.adaptivealerting.tools.pipeline.StreamPublisherSupport;
import com.expedia.adaptivealerting.tools.pipeline.StreamSubscriber;
import com.expedia.www.haystack.commons.entities.MetricPoint;

import java.util.Timer;
import java.util.TimerTask;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Abstract base class for implementing metric sources. Uses a timer to simulate timing behavior.
 *
 * @author Willie Wheeler
 */
public abstract class AbstractMetricSource {
    private final String name;
    private final long period;
    
    private final Timer timer = new Timer();
    private final StreamPublisherSupport<MetricPoint> publisherSupport = new StreamPublisherSupport<>();
    
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
    
    public void start() {
        timer.scheduleAtFixedRate(new TimerTask() {
            
            @Override
            public void run() {
                final MetricPoint next = next();
                if (next != null) {
                    publisherSupport.publish(next);
                }
            }
        }, 0L, period);
    }
    
    /**
     * Returns the next message in the stream.
     *
     * @return Next metric point.
     */
    public abstract MetricPoint next();
    
    public void stop() {
        timer.cancel();
        timer.purge();
    }
    
    public void addMetricPointSubscriber(StreamSubscriber<MetricPoint> subscriber) {
        notNull(subscriber, "subscriber can't be null");
        publisherSupport.addSubscriber(subscriber);
    }
    
    public void removeMetricPointSubscriber(StreamSubscriber<MetricPoint> subscriber) {
        notNull(subscriber, "subscriber can't be null");
        publisherSupport.removeSubscriber(subscriber);
    }
}
