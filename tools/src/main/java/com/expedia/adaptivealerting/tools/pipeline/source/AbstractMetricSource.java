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

import com.expedia.adaptivealerting.tools.pipeline.util.MetricPointSubscriber;
import com.expedia.www.haystack.commons.entities.MetricPoint;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.expedia.adaptivealerting.core.util.AssertUtil.isTrue;
import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Abstract base class for implementing metric sources. Uses a timer to simulate timing behavior.
 *
 * @author Willie Wheeler
 */
public abstract class AbstractMetricSource implements MetricSource {
    private final String metricName;
    private final long period;
    private final List<MetricPointSubscriber> subscribers = new LinkedList<>();

    private final Timer timer = new Timer();

    /**
     * Creates a new metric source with the given period.
     *
     * @param metricName
     *            Metric metricName.
     * @param period
     *            Timer period in seconds.
     */
    public AbstractMetricSource(String metricName, long period) {
        notNull(metricName, "metricName can't be null");
        isTrue(period > 0, "period must be > 0");

        this.metricName = metricName;
        this.period = period;
    }

    @Override
    public String getMetricName() {
        return metricName;
    }

    @Override
    public void start() {
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                final MetricPoint next = next();
                if (next != null) {
                    publish(next);
                }
            }
        }, 0L, period);
    }

    @Override
    public void stop() {
        timer.cancel();
        timer.purge();
    }

    @Override
    public void addSubscriber(MetricPointSubscriber subscriber) {
        notNull(subscriber, "subscriber can't be null");
        subscribers.add(subscriber);
    }

    @Override
    public void removeSubscriber(MetricPointSubscriber subscriber) {
        notNull(subscriber, "subscriber can't be null");
        subscribers.remove(subscriber);
    }

    private void publish(MetricPoint metricPoint) {
        subscribers.stream().forEach(subscriber -> subscriber.next(metricPoint));
    }
}
