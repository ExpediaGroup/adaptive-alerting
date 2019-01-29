/*
 * Copyright 2019 Expedia Group, Inc.
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
package com.expedia.adaptivealerting.samples;

import com.expedia.adaptivealerting.core.data.MetricFrame;
import com.expedia.adaptivealerting.core.data.io.MetricFrameLoader;
import com.expedia.adaptivealerting.tools.pipeline.source.MetricFrameMetricSource;
import com.expedia.metrics.MetricDefinition;

import java.io.IOException;
import java.io.InputStream;

public class MetricGenerationHelper {
    // TODO: Use this from other Sample classes
    public static MetricFrameMetricSource buildMetricFrameMetricSource(String filename, long periodMs) throws IOException {
        final InputStream is = ClassLoader.getSystemResourceAsStream(filename);
        // TODO Use the FileDataConnector rather than the MetricFrameLoader. [WLW]
        final MetricFrame frame = MetricFrameLoader.loadCsv(new MetricDefinition("csv"), is, true);
        return new MetricFrameMetricSource(frame, "data", periodMs);
    }
}
