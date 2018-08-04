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
import com.expedia.adaptivealerting.dataconnect.MetricFrameLoader;
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
public class CsvTrafficPewmaVariants {
    
    public static void main(String[] args) throws Exception {
    
        // TODO Use the FileDataConnector rather than the MetricFrameLoader. [WLW]
        final InputStream is = ClassLoader.getSystemResourceAsStream("samples/cal-inflow.csv");
        final MetricFrame frame = MetricFrameLoader.loadCsv(new Metric(), is, true);
        final MetricFrameMetricSource source = new MetricFrameMetricSource(frame, "data", 200L);
        
        final AnomalyDetectorFilter ad1 =
                new AnomalyDetectorFilter(new PewmaAnomalyDetector(0.15, 1.0, 2.0, 3.0, 0.0));
        final AnomalyDetectorFilter ad2 =
                new AnomalyDetectorFilter(new PewmaAnomalyDetector(0.25, 1.0, 2.0, 3.0, 0.0));
        final AnomalyDetectorFilter ad3 =
                new AnomalyDetectorFilter(new PewmaAnomalyDetector(0.35, 1.0, 2.0, 3.0, 0.0));
    
        final EvaluatorFilter eval1 = new EvaluatorFilter(new RmseEvaluator());
        final EvaluatorFilter eval2 = new EvaluatorFilter(new RmseEvaluator());
        final EvaluatorFilter eval3 = new EvaluatorFilter(new RmseEvaluator());
        
        final AnomalyChartSink chart1 = PipelineFactory.createChartSink("PEWMA: alpha=0.15");
        final AnomalyChartSink chart2 = PipelineFactory.createChartSink("PEWMA: alpha=0.25");
        final AnomalyChartSink chart3 = PipelineFactory.createChartSink("PEWMA: alpha=0.35");
    
        source.addSubscriber(ad1);
        source.addSubscriber(ad2);
        source.addSubscriber(ad3);
    
        ad1.addSubscriber(eval1);
        ad2.addSubscriber(eval2);
        ad3.addSubscriber(eval3);
        
        ad1.addSubscriber(chart1);
        ad2.addSubscriber(chart2);
        ad3.addSubscriber(chart3);
        
        eval1.addSubscriber(chart1);
        eval2.addSubscriber(chart2);
        eval3.addSubscriber(chart3);
        
        showChartFrame(createChartFrame("Cal Inflow", chart1.getChart(), chart2.getChart(), chart3.getChart()));
        source.start();
    }
}
