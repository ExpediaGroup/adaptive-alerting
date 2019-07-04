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

import com.expedia.adaptivealerting.anomdetect.detect.AnomalyType;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.ForecastingDetector;
import com.expedia.adaptivealerting.anomdetect.forecast.eval.algo.RmsePointForecastEvaluator;
import com.expedia.adaptivealerting.anomdetect.forecast.point.algo.PewmaPointForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.point.algo.PewmaPointForecasterParams;
import com.expedia.adaptivealerting.anomdetect.forecast.interval.algo.ExponentialWelfordIntervalForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.interval.algo.ExponentialWelfordIntervalForecasterParams;
import com.expedia.adaptivealerting.anomdetect.util.MetricFrameLoader;
import com.expedia.adaptivealerting.tools.pipeline.filter.DetectorFilter;
import com.expedia.adaptivealerting.tools.pipeline.filter.EvaluatorFilter;
import com.expedia.adaptivealerting.tools.pipeline.source.MetricFrameMetricSource;
import com.expedia.adaptivealerting.tools.pipeline.util.PipelineFactory;
import com.expedia.metrics.MetricDefinition;
import lombok.val;

import java.util.UUID;

import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.createChartFrame;
import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.showChartFrame;

public class CsvTrafficPewmaVariants {

    public static void main(String[] args) throws Exception {
        val is = ClassLoader.getSystemResourceAsStream("samples/cal-inflow.csv");
        val metricFrame = MetricFrameLoader.loadCsv(new MetricDefinition("csv"), is, true);
        val metricSource = new MetricFrameMetricSource(metricFrame, "data", 200L);

        val detectorFilter1 = buildDetectorFilter(0.15, 1.0, 0.0, 2.0, 3.0);
        val detectorFilter2 = buildDetectorFilter(0.25, 1.0, 0.0, 2.0, 3.0);
        val detectorFilter3 = buildDetectorFilter(0.35, 1.0, 0.0, 2.0, 3.0);

        val eval1 = new EvaluatorFilter(new RmsePointForecastEvaluator());
        val eval2 = new EvaluatorFilter(new RmsePointForecastEvaluator());
        val eval3 = new EvaluatorFilter(new RmsePointForecastEvaluator());

        val chart1 = PipelineFactory.createChartSink("PEWMA: alpha=0.15");
        val chart2 = PipelineFactory.createChartSink("PEWMA: alpha=0.25");
        val chart3 = PipelineFactory.createChartSink("PEWMA: alpha=0.35");

        metricSource.addSubscriber(detectorFilter1);
        metricSource.addSubscriber(detectorFilter2);
        metricSource.addSubscriber(detectorFilter3);

        detectorFilter1.addSubscriber(eval1);
        detectorFilter2.addSubscriber(eval2);
        detectorFilter3.addSubscriber(eval3);

        detectorFilter1.addSubscriber(chart1);
        detectorFilter2.addSubscriber(chart2);
        detectorFilter3.addSubscriber(chart3);

        eval1.addSubscriber(chart1);
        eval2.addSubscriber(chart2);
        eval3.addSubscriber(chart3);

        showChartFrame(createChartFrame("Cal Inflow", chart1.getChart(), chart2.getChart(), chart3.getChart()));
        metricSource.start();
    }

    private static DetectorFilter buildDetectorFilter(
            double alpha,
            double beta,
            double initMeanEstimate,
            double weakSigmas,
            double strongSigmas) {

        val pewmaParams = new PewmaPointForecasterParams()
                .setAlpha(alpha)
                .setBeta(beta)
                .setInitMeanEstimate(initMeanEstimate);
        val pewma = new PewmaPointForecaster(pewmaParams);

        val welfordParams = new ExponentialWelfordIntervalForecasterParams()
                .setAlpha(alpha)
                .setWeakSigmas(weakSigmas)
                .setStrongSigmas(strongSigmas);
        val welford = new ExponentialWelfordIntervalForecaster(welfordParams);

        val detector = new ForecastingDetector(UUID.randomUUID(), pewma, welford, AnomalyType.RIGHT_TAILED);
        return new DetectorFilter(detector);
    }
}
