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

import com.expedia.adaptivealerting.core.data.MetricFrame;
import com.expedia.adaptivealerting.core.util.ThreadUtil;
import com.expedia.adaptivealerting.tools.pipeline.util.MetricDataSubscriber;
import com.expedia.adaptivealerting.tools.util.TestObjectMother;
import com.expedia.metrics.MetricData;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertEquals;

@Slf4j
public final class MetricFrameMetricSourceTest {
    private MetricFrameMetricSource sourceUnderTest;
    private MetricFrame frame;

    @Before
    public void setUp() {
        val metricDef = TestObjectMother.metricDefinition();
        val metricData = new MetricData(metricDef, 10.0, Instant.now().getEpochSecond());
        val metricDataPoints = new MetricData[]{metricData};
        this.frame = new MetricFrame(metricDataPoints);
        this.sourceUnderTest = new MetricFrameMetricSource(frame, "my-metric-def", 100L);
    }

    @Test
    public void testNext() {
        int[] count = new int[1];
        val subscriber = new MetricDataSubscriber() {
            @Override
            public void next(MetricData metricData) {
                log.info("Next!!");
                count[0]++;
            }
        };
        sourceUnderTest.addSubscriber(subscriber);
        sourceUnderTest.start();
        ThreadUtil.sleep(500L);
        sourceUnderTest.stop();
        sourceUnderTest.removeSubscriber(subscriber);
        assertEquals(frame.getNumRows(), count[0]);
    }
}
