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
package com.expedia.adaptivealerting.samples;

import com.expedia.adaptivealerting.core.detector.PewmaOutlierDetector;
import com.expedia.adaptivealerting.tools.pipeline.MetricFilter;
import com.expedia.adaptivealerting.tools.pipeline.MetricSink;
import com.expedia.adaptivealerting.tools.pipeline.MetricSource;
import com.expedia.adaptivealerting.tools.pipeline.filter.OutlierDetectorMetricFilter;
import com.expedia.adaptivealerting.tools.pipeline.sink.ConsoleLogMetricSink;
import com.expedia.adaptivealerting.tools.pipeline.source.WhiteNoiseMetricSource;

/**
 * This is a sample pipeline based on white noise and a PEWMA filter.
 *
 * @author Willie Wheeler
 */
public class WhiteNoisePipeline {
    
    public static void main(String[] args) {
        final MetricSource source = new WhiteNoiseMetricSource();
        
        final MetricFilter filter = new OutlierDetectorMetricFilter(new PewmaOutlierDetector());
        source.addSubscriber(filter);
        
        final MetricSink sink = new ConsoleLogMetricSink();
        filter.addSubscriber(sink);
        
        source.start();
    }
}
