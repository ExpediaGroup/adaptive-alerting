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
import com.expedia.www.haystack.commons.entities.MetricType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Enumeration;
import scala.collection.immutable.Map;
import scala.collection.immutable.Map$;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.sqrt;

/**
 * <p>
 * Generates an infinite series of white noise {@link com.expedia.www.haystack.commons.entities.MetricPoint}s at a given
 * rate. The data distribution is Gaussian and configurable. The rate is likewise configurable.
 * </p>
 * <p>
 * This class is primarily intended to support testing.
 * </p>
 *
 * @author Willie Wheeler
 */
public final class WhiteNoiseMetricSource implements MetricSource {
    private static final Logger log = LoggerFactory.getLogger(WhiteNoiseMetricSource.class);
    
    private final String name;
    private final double mean;
    private final double variance;
    private final long period;
    
    private Timer timer;
    
    /**
     * Creates a new, normally-distributed white noise generator. The series name is "white noise" and the period is one
     * second.
     */
    public WhiteNoiseMetricSource() {
        this("white-noise", 0.0, 1.0, 1000L);
    }
    
    /**
     * Creates a new white noise data generator.
     *
     * @param name     Series name.
     * @param mean     Gaussian mean.
     * @param variance Gaussian variance.
     * @param period   Tick period in milliseconds.
     */
    public WhiteNoiseMetricSource(String name, double mean, double variance, long period) {
        this.name = name;
        this.mean = mean;
        this.variance = variance;
        this.period = period;
    }
    
    @Override
    public void start(MetricSourceCallback callback) {
        final Random random = new Random();
        final double stdDev = sqrt(variance);
        final Enumeration.Value type = MetricType.Gauge();
        final Map<String, String> tags = Map$.MODULE$.<String, String>empty();
    
        this.timer = new Timer();
        log.info("Starting WhiteNoiseMetricSource");
        timer.scheduleAtFixedRate(new TimerTask() {
            
            @Override
            public void run() {
                final float value = (float) (stdDev * random.nextGaussian() + mean);
                final long now = System.currentTimeMillis() / 1000;
                callback.next(new MetricPoint(name, type, tags, value, now));
            }
        }, 0L, period);
    }
    
    @Override
    public void stop() {
        log.info("Stopping WhiteNoiseMetricSource");
        timer.cancel();
        timer.purge();
        this.timer = null;
    }
}
