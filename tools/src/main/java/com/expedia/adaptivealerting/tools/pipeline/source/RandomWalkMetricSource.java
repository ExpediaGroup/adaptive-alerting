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

import com.expedia.www.haystack.commons.entities.MetricPoint;

import java.time.Instant;
import java.util.Random;

import static com.expedia.adaptivealerting.core.util.MetricPointUtil.metricPoint;

/**
 * Generates an infinite series based on a <a href="https://en.wikipedia.org/wiki/Random_walk">random walk</a>.
 *
 * @author Willie Wheeler
 */
public final class RandomWalkMetricSource extends AbstractMetricSource {
    private int currentValue = 0;
    private final Random random = new Random();
    
    /**
     * Creates a new random walk metric source with name "random-walk", period=1000 and startValue=0.
     */
    public RandomWalkMetricSource() {
        this("random-walk", 1000L, 0);
    }
    
    public RandomWalkMetricSource(String name, long period, int startValue) {
        super(name, period);
        this.currentValue = startValue;
    }
    
    @Override
    public MetricPoint next() {
        final MetricPoint result = metricPoint(getMetricName(), Instant.now(), currentValue);
        final int movement = 1 - random.nextInt(3);
        this.currentValue += movement;
        return result;
    }
}
