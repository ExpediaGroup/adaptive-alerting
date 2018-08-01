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
package com.expedia.adaptivealerting.core.io;

import com.expedia.adaptivealerting.core.data.Metric;
import com.expedia.adaptivealerting.core.data.MetricFrame;
import com.expedia.adaptivealerting.core.data.repo.MetricFrameLoader;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Willie Wheeler
 */
public final class MetricFrameLoaderTest {
    private static final double TOLERANCE = 0.001;
    
    @Test
    public void testLoadCsv() throws Exception {
        final Metric metric = new Metric();
        final InputStream is = ClassLoader.getSystemResourceAsStream("datasets/cal-inflow.csv");
        final MetricFrame frame = MetricFrameLoader.loadCsv(metric, is, true);
        assertNotNull(frame);
        assertTrue(frame.getNumRows() > 0);
        assertEquals(0.0, frame.getMetricPoint(0).getValue(), TOLERANCE);
        assertEquals(3.0, frame.getMetricPoint(15).getValue(), TOLERANCE);
    }
}
