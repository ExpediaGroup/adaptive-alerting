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
package com.expedia.adaptivealerting.anomdetect.breakout;

import com.expedia.adaptivealerting.anomdetect.util.MetricFrameLoader;
import com.expedia.adaptivealerting.anomdetect.util.TestObjectMother;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

public final class EdmxBreakoutDetectorTest {
    private EdmxBreakoutDetector detectorUnderTest;

    @Before
    public void setUp() {
        this.detectorUnderTest = new EdmxBreakoutDetector(UUID.randomUUID());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDetect_nullMetricData() {
        detectorUnderTest.detect(null);
    }

    // TODO Support a single detector managing state for multiple metrics

    @Test
    public void testDetect_whiteNoiseWithBreakout() throws Exception {
        val metricDef = TestObjectMother.metricDefinition();
        val is = ClassLoader.getSystemResourceAsStream("datasets/white-noise-with-breakout-at-row-600.csv");
        val metricFrame = MetricFrameLoader.loadCsv(metricDef, is, false);
        val metricDataList = metricFrame.getMetricData();
        for (val metricData : metricDataList) {
            detectorUnderTest.detect(metricData);
        }
    }
}
