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
package com.expedia.adaptivealerting.tools.metricsource;

import com.expedia.adaptivealerting.core.util.MetricPointUtil;
import com.expedia.www.haystack.commons.entities.MetricPoint;

import java.time.Instant;
import java.util.Random;

import static java.lang.Math.sqrt;

/**
 * Generates an infinite series of white noise {@link com.expedia.www.haystack.commons.entities.MetricPoint}s at a given
 * rate. The data distribution is Gaussian and configurable.
 *
 * @author Willie Wheeler
 */
public final class WhiteNoiseMetricSource extends AbstractMetricSource {
    private final double mean;
    private final double stdDev;
    private final Random random = new Random();
    
    /**
     * Creates a new, normally-distributed white noise generator. The series name is "white noise" and the period is one
     * second.
     */
    public WhiteNoiseMetricSource() {
        this("white-noise", 1000L, 0.0, 1.0);
    }
    
    /**
     * Creates a new white noise data generator.
     *
     * @param name     Series name.
     * @param period   Timer period in milliseconds.
     * @param mean     Gaussian mean.
     * @param variance Gaussian variance.
     */
    public WhiteNoiseMetricSource(String name, long period, double mean, double variance) {
        super(name, period);
        
        this.mean = mean;
        this.stdDev = sqrt(variance);
    }
    
    @Override
    public MetricPoint next() {
        final float value = (float) (stdDev * random.nextGaussian() + mean);
        return MetricPointUtil.metricPoint(getName(), Instant.now(), value);
    }
}
