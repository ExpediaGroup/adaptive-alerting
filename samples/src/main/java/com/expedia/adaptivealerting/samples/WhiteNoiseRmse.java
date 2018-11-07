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

import com.expedia.adaptivealerting.anomdetect.ewma.EwmaAnomalyDetector;
import com.expedia.adaptivealerting.anomdetect.pewma.PewmaAnomalyDetector;
import com.expedia.adaptivealerting.core.evaluator.RmseEvaluator;
import com.expedia.adaptivealerting.tools.pipeline.filter.AnomalyDetectorFilter;
import com.expedia.adaptivealerting.tools.pipeline.filter.EvaluatorFilter;
import com.expedia.adaptivealerting.tools.pipeline.sink.AnomalyChartSink;
import com.expedia.adaptivealerting.tools.pipeline.source.WhiteNoiseMetricSource;
import com.expedia.adaptivealerting.tools.pipeline.util.PipelineFactory;

import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.createChartFrame;
import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.showChartFrame;

// Disabled CUSUM and individuals as they don't have natural predictions. [WLW]

/**
 * This is a sample pipeline to calculate RMSE
 *
 * @author Karan Shah
 */
public class WhiteNoiseRmse {
    
    public static void main(String[] args) {
        final WhiteNoiseMetricSource source = new WhiteNoiseMetricSource("white-noise", 1000L, 0.0, 1.0);
        
        final AnomalyDetectorFilter ewmaFilter = new AnomalyDetectorFilter(new EwmaAnomalyDetector());
        final AnomalyDetectorFilter pewmaFilter = new AnomalyDetectorFilter(new PewmaAnomalyDetector());
//        final AnomalyDetectorFilter cusumFilter = new AnomalyDetectorFilter(new CusumAnomalyDetector());
//        final AnomalyDetectorFilter shewhartIndividualsFilter =
//                new AnomalyDetectorFilter(new IndividualsControlChartAnomalyDetector());
        
        final EvaluatorFilter ewmaEval = new EvaluatorFilter(new RmseEvaluator());
        final EvaluatorFilter pewmaEval = new EvaluatorFilter(new RmseEvaluator());
//        final EvaluatorFilter cusumEval = new EvaluatorFilter(new RmseEvaluator());
//        final EvaluatorFilter individualChartEval = new EvaluatorFilter(new RmseEvaluator());
        
        final AnomalyChartSink ewmaChart = PipelineFactory.createChartSink("EWMA");
        final AnomalyChartSink pewmaChart = PipelineFactory.createChartSink("PEWMA");
//        final AnomalyChartSink cusumChart = PipelineFactory.createChartSink("CUSUM");
//        final AnomalyChartSink individualsChart = PipelineFactory.createChartSink("Shewhart Individuals");
        
        source.addSubscriber(ewmaFilter);
        ewmaFilter.addSubscriber(ewmaEval);
        ewmaFilter.addSubscriber(ewmaChart);
        ewmaEval.addSubscriber(ewmaChart);
        
        source.addSubscriber(pewmaFilter);
        pewmaFilter.addSubscriber(pewmaEval);
        pewmaFilter.addSubscriber(pewmaChart);
        pewmaEval.addSubscriber(pewmaChart);

//        source.addSubscriber(cusumFilter);
//        cusumFilter.addSubscriber(cusumEval);
//        cusumFilter.addSubscriber(cusumChart);
//        cusumEval.addSubscriber(cusumChart);
//
//        source.addSubscriber(shewhartIndividualsFilter);
//        shewhartIndividualsFilter.addSubscriber(individualChartEval);
//        shewhartIndividualsFilter.addSubscriber(individualsChart);
//        individualChartEval.addSubscriber(individualsChart);
        
        showChartFrame(createChartFrame(
                "White Noise RMSE",
                ewmaChart.getChart(),
                pewmaChart.getChart()
//                cusumChart.getChart(),
//                individualsChart.getChart()
        ));
        source.start();
    }
}
