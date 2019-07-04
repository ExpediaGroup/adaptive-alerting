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
package com.expedia.adaptivealerting.anomdetect.detect.breakout.algo;

import com.expedia.adaptivealerting.anomdetect.detect.breakout.algo.EdmxDetector;
import com.expedia.adaptivealerting.anomdetect.detect.breakout.algo.EdmxDetectorResult;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.EdmxHyperparams;
import com.expedia.adaptivealerting.anomdetect.util.MetricFrameLoader;
import com.expedia.adaptivealerting.anomdetect.util.TestObjectMother;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;

import java.util.UUID;

@Slf4j
public final class EdmxDetectorTest {

    // TODO Test warmup
    // TODO Support a single detector managing state for multiple metrics?

    @Test
    public void testDetect_whiteNoiseWithBreakout() throws Exception {
        val hyperparams = new EdmxHyperparams()
                .setBufferSize(20)
                .setDelta(6)
                .setNumPerms(199)
                .setAlpha(0.01);
        val detectorUnderTest = new EdmxDetector(UUID.randomUUID(), hyperparams);

        val metricDef = TestObjectMother.metricDefinition();
        val is = ClassLoader.getSystemResourceAsStream("datasets/white-noise-with-breakout-at-row-600.csv");
        val metricFrame = MetricFrameLoader.loadCsv(metricDef, is, false);
        val metricDataList = metricFrame.getMetricData();

        for (int i = 0; i < 700; i++) {
            val metricData = metricDataList.get(i);
            val result = (EdmxDetectorResult) detectorUnderTest.detect(metricData);
            if (!result.isWarmup() && result.getTimestamp() != null && result.getSignificant()) {
//                log.trace("row={}: timestamp={}, pValue={}",
//                        i + 1,
//                        result.getTimestamp(),
//                        result.getPValue());
                log.trace("row={}: {}", i + 1, result);
            }
        }
    }
}
