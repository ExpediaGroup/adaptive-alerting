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
import org.junit.Test;

import java.util.concurrent.Executors;

import static junit.framework.TestCase.assertTrue;

/**
 * @author Willie Wheeler
 */
public class WhiteNoiseMetricSourceTest {
    
    @Test
    public void testStartAndStop() throws Exception {
        final boolean[] calledNext = new boolean[1];
        
        final MetricSubscriber subscriber = new MetricSubscriber() {
            
            @Override
            public void next(MetricPoint metricPoint) {
                calledNext[0] = true;
            }
        };
        
        final WhiteNoiseMetricSource metricSource = new WhiteNoiseMetricSource();
        metricSource.addSubscriber(subscriber);
        
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            
            @Override
            public void run() {
                metricSource.start();
            }
        });
        Thread.sleep(1000L);
        metricSource.stop();
        metricSource.removeSubscriber(subscriber);
    
        assertTrue(calledNext[0]);
    }
}
