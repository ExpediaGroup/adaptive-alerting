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
package com.expedia.adaptivealerting.core.data.io;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * {@link MetricFrameLoader} unit test.
 */
@Slf4j
public class MetricFrameLoaderTest {

    @Test
    public void testLoadCsv() throws IOException {
        val defFilename = ClassLoader.getSystemResource("datasets/cal-inflow-metric-def.json").getFile();
        val dataFilename = ClassLoader.getSystemResource("datasets/cal-inflow.csv").getFile();
        val defFile = new File(defFilename);
        val dataFile = new File(dataFilename);
        log.info("defFile={}, dataFile={}", defFile, dataFile);
        val metricFrame = MetricFrameLoader.loadCsv(defFile, dataFile, true);
        assertEquals(5040, metricFrame.getNumRows());
        assertEquals(9.0, metricFrame.getMetricData().get(118).getValue(), 0.001);
    }
}
