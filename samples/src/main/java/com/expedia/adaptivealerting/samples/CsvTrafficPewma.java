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

import com.expedia.adaptivealerting.anomdetect.PewmaAnomalyDetector;
import com.expedia.adaptivealerting.core.data.Metric;
import com.expedia.adaptivealerting.core.data.MetricFrame;
import com.expedia.adaptivealerting.core.evaluator.RmseEvaluator;
import com.expedia.adaptivealerting.core.io.MetricFrameLoader;
import com.expedia.adaptivealerting.tools.pipeline.filter.AnomalyDetectorFilter;
import com.expedia.adaptivealerting.tools.pipeline.filter.EvaluatorFilter;
import com.expedia.adaptivealerting.tools.pipeline.sink.AnomalyChartSink;
import com.expedia.adaptivealerting.tools.pipeline.source.MetricFrameMetricSource;
import com.expedia.adaptivealerting.tools.pipeline.util.PipelineFactory;

import java.io.InputStream;

import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.createChartFrame;
import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.showChartFrame;

/**
 * @author Willie Wheeler
 */
public final class CsvTrafficPewma {
    
    public static void main(String[] args) throws Exception {
        final InputStream is = ClassLoader.getSystemResourceAsStream("samples/cal-inflow.csv");
        final MetricFrame frame = MetricFrameLoader.loadCsv(new Metric(), is, true);
        final MetricFrameMetricSource source = new MetricFrameMetricSource(frame, "data", 200L);
        
        final AnomalyDetectorFilter detector = new AnomalyDetectorFilter(new PewmaAnomalyDetector());
        final EvaluatorFilter evaluator = new EvaluatorFilter(new RmseEvaluator());
        final AnomalyChartSink chartWrapper = PipelineFactory.createChartSink("PEWMA");
        
        source.addSubscriber(detector);
        detector.addSubscriber(evaluator);
        detector.addSubscriber(chartWrapper);
        evaluator.addSubscriber(chartWrapper);
        
        showChartFrame(createChartFrame("Cal Inflow", chartWrapper.getChart()));
        source.start();
    }
}
