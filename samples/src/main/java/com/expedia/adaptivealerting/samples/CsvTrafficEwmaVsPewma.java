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

import com.expedia.adaptivealerting.anomdetect.EwmaAnomalyDetector;
import com.expedia.adaptivealerting.anomdetect.PewmaAnomalyDetector;
import com.expedia.adaptivealerting.core.data.Metric;
import com.expedia.adaptivealerting.core.data.MetricFrame;
import com.expedia.adaptivealerting.core.evaluator.RmseEvaluator;
import com.expedia.adaptivealerting.core.data.repo.MetricFrameLoader;
import com.expedia.adaptivealerting.tools.pipeline.filter.AnomalyDetectorFilter;
import com.expedia.adaptivealerting.tools.pipeline.filter.EvaluatorFilter;
import com.expedia.adaptivealerting.tools.pipeline.sink.AnomalyChartSink;
import com.expedia.adaptivealerting.tools.pipeline.source.MetricFrameMetricSource;
import com.expedia.adaptivealerting.tools.pipeline.util.PipelineFactory;

import java.io.InputStream;

import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.createChartFrame;
import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.showChartFrame;

/**
 * Sample that creates a pipeline for traffic data sourced from a CSV file. We have both EWMA and PEWMA charts, both
 * with RMSE evaluators.
 *
 * @author Willie Wheeler
 */
public final class CsvTrafficEwmaVsPewma {
    
    public static void main(String[] args) throws Exception {
        final InputStream is = ClassLoader.getSystemResourceAsStream("samples/cal-inflow.csv");
        final MetricFrame frame = MetricFrameLoader.loadCsv(new Metric(), is, true);
        final MetricFrameMetricSource source = new MetricFrameMetricSource(frame, "data", 200L);
        
        final AnomalyDetectorFilter ewmaAD = new AnomalyDetectorFilter(new EwmaAnomalyDetector());
        final AnomalyDetectorFilter pewmaAD = new AnomalyDetectorFilter(new PewmaAnomalyDetector());
    
        final EvaluatorFilter ewmaEval = new EvaluatorFilter(new RmseEvaluator());
        final EvaluatorFilter pewmaEval = new EvaluatorFilter(new RmseEvaluator());
        
        final AnomalyChartSink ewmaChart = PipelineFactory.createChartSink("EWMA");
        final AnomalyChartSink pewmaChart = PipelineFactory.createChartSink("PEWMA");
        
        source.addSubscriber(ewmaAD);
        ewmaAD.addSubscriber(ewmaEval);
        ewmaAD.addSubscriber(ewmaChart);
        ewmaEval.addSubscriber(ewmaChart);
        
        source.addSubscriber(pewmaAD);
        pewmaAD.addSubscriber(pewmaEval);
        pewmaAD.addSubscriber(pewmaChart);
        pewmaEval.addSubscriber(pewmaChart);
        
        showChartFrame(createChartFrame("Cal Inflow", ewmaChart.getChart(), pewmaChart.getChart()));
        source.start();
    }
}
