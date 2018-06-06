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

import com.expedia.adaptivealerting.core.util.ThreadUtil;
import com.expedia.adaptivealerting.tools.pipeline.MetricPointSubscriber;
import com.expedia.www.haystack.commons.entities.MetricPoint;

import java.util.concurrent.Executors;

import static junit.framework.TestCase.assertTrue;

/**
 * Support for writing metric source unit tests.
 *
 * @author Willie Wheeler
 */
public final class MetricSourceTestSupport {
    
    /**
     * Exercises the start/stop methods, and verifies that the source called next().
     *
     * @param metricSource Metric source.
     * @param runMillis    How long to let the source run, in milliseconds, before stopping it. Be sure this is long
     *                     enough for the source to call next(). (This is particularly important for timer-based
     *                     sources.)
     */
    public void testStartAndStop(MetricSource metricSource, long runMillis) {
        final boolean[] calledNext = new boolean[1];
        
        final MetricPointSubscriber subscriber = new MetricPointSubscriber() {
            @Override
            public void next(MetricPoint metricPoint) {
                calledNext[0] = true;
            }
        };
        
        metricSource.addSubscriber(subscriber);
        
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                metricSource.start();
            }
        });
        
        ThreadUtil.sleep(runMillis);
        metricSource.stop();
        metricSource.removeSubscriber(subscriber);
    
        assertTrue(calledNext[0]);
    }
}
