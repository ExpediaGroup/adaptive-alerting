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
package com.expedia.adaptivealerting.samples;

import com.expedia.adaptivealerting.anomdetect.forecast.point.EwmaDetector;
import com.expedia.adaptivealerting.anomdetect.forecast.point.PewmaDetector;
import com.expedia.adaptivealerting.core.evaluator.RmseEvaluator;
import com.expedia.adaptivealerting.tools.pipeline.filter.AnomalyDetectorFilter;
import com.expedia.adaptivealerting.tools.pipeline.filter.EvaluatorFilter;
import com.expedia.adaptivealerting.tools.pipeline.source.WhiteNoiseMetricSource;
import com.expedia.adaptivealerting.tools.pipeline.util.PipelineFactory;
import lombok.val;

import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.createChartFrame;
import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.showChartFrame;

// Disabled CUSUM and individuals as they don't have natural predictions. [WLW]

/**
 * This is a sample pipeline to calculate RMSE
 */
public class WhiteNoiseRmse {

    public static void main(String[] args) {
        val source = new WhiteNoiseMetricSource("white-noise", 1000L, 0.0, 1.0);

        val ewmaFilter = new AnomalyDetectorFilter(new EwmaDetector());
        val pewmaFilter = new AnomalyDetectorFilter(new PewmaDetector());
//        val cusumFilter = new AnomalyDetectorFilter(new CusumDetector());
//        val shewhartIndividualsFilter =
//                new AnomalyDetectorFilter(new IndividualsControlChartDetector());

        val ewmaEval = new EvaluatorFilter(new RmseEvaluator());
        val pewmaEval = new EvaluatorFilter(new RmseEvaluator());
//        val cusumEval = new EvaluatorFilter(new RmseEvaluator());
//        val individualChartEval = new EvaluatorFilter(new RmseEvaluator());

        val ewmaChart = PipelineFactory.createChartSink("EWMA");
        val pewmaChart = PipelineFactory.createChartSink("PEWMA");
//        val cusumChart = PipelineFactory.createChartSink("CUSUM");
//        val individualsChart = PipelineFactory.createChartSink("Shewhart Individuals");

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
