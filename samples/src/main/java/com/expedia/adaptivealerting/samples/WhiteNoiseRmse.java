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

import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.createChartFrame;
import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.showChartFrame;
import com.expedia.adaptivealerting.anomdetect.EwmaAnomalyDetector;
import com.expedia.adaptivealerting.core.evaluator.RmseEvaluator;
import com.expedia.adaptivealerting.tools.pipeline.filter.AnomalyDetectorFilter;
import com.expedia.adaptivealerting.tools.pipeline.filter.EvaluatorFilter;
import com.expedia.adaptivealerting.tools.pipeline.sink.AnomalyChartSink;
import com.expedia.adaptivealerting.tools.pipeline.source.WhiteNoiseMetricSource;
import com.expedia.adaptivealerting.tools.pipeline.util.PipelineFactory;

/**
 * This is a sample pipeline to calculate RMSE
 *
 * @author Karan Shah
 */
public class WhiteNoiseRmse {

    public static void main(String[] args) {
        final WhiteNoiseMetricSource source = new WhiteNoiseMetricSource("white-noise", 1000L, 0.0, 1.0);

        final AnomalyDetectorFilter ewmaFilter = new AnomalyDetectorFilter(new EwmaAnomalyDetector());
        final EvaluatorFilter evaluatorFilter = new EvaluatorFilter(new RmseEvaluator());
        source.addSubscriber(ewmaFilter);
        // Evaluator listening to the detector
        ewmaFilter.addSubscriber(evaluatorFilter);

        final AnomalyChartSink ewmaChart = PipelineFactory.createChartSink("EWMA");
        final AnomalyChartSink rmseChart = PipelineFactory.createChartSink("White Noise RMSE");

        ewmaFilter.addSubscriber(ewmaChart);
        evaluatorFilter.addSubscriber(rmseChart);

        showChartFrame(createChartFrame("White Noise EMWA RMSE", ewmaChart.getChart(), rmseChart.getChart()));
        source.start();
    }
}
