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

import com.expedia.adaptivealerting.anomdetect.forecast.eval.algo.RmsePointForecastEvaluator;
import com.expedia.adaptivealerting.anomdetect.util.MetricFrameLoader;
import com.expedia.adaptivealerting.samples.util.DetectorUtil;
import com.expedia.adaptivealerting.tools.pipeline.filter.DetectorFilter;
import com.expedia.adaptivealerting.tools.pipeline.filter.EvaluatorFilter;
import com.expedia.adaptivealerting.tools.pipeline.source.MetricFrameMetricSource;
import com.expedia.adaptivealerting.tools.pipeline.util.PipelineFactory;
import com.expedia.metrics.MetricDefinition;
import lombok.val;

import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.createChartFrame;
import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.showChartFrame;

/**
 * Sample that creates a pipeline for traffic data sourced from a CSV file. We have both EWMA and PEWMA charts, both
 * with RMSE evaluators.
 */
public final class CsvTrafficEwmaVsPewma {

    public static void main(String[] args) throws Exception {
        val is = ClassLoader.getSystemResourceAsStream("samples/cal-inflow.csv");
        val metricFrame = MetricFrameLoader.loadCsv(new MetricDefinition("csv"), is, true);
        val metricSource = new MetricFrameMetricSource(metricFrame, "data", 200L);

        val ewmaDetector = DetectorUtil.buildEwmaDetector();
        val ewmaFilter = new DetectorFilter(ewmaDetector);
        val ewmaEval = new EvaluatorFilter(new RmsePointForecastEvaluator());
        val ewmaChart = PipelineFactory.createChartSink("EWMA");

        metricSource.addSubscriber(ewmaFilter);
        ewmaFilter.addSubscriber(ewmaEval);
        ewmaFilter.addSubscriber(ewmaChart);
        ewmaEval.addSubscriber(ewmaChart);

        val pewmaDetector = DetectorUtil.buildPewmaDetector();
        val pewmaFilter = new DetectorFilter(pewmaDetector);
        val pewmaEval = new EvaluatorFilter(new RmsePointForecastEvaluator());
        val pewmaChart = PipelineFactory.createChartSink("PEWMA");

        metricSource.addSubscriber(pewmaFilter);
        pewmaFilter.addSubscriber(pewmaEval);
        pewmaFilter.addSubscriber(pewmaChart);
        pewmaEval.addSubscriber(pewmaChart);

        showChartFrame(createChartFrame("Cal Inflow", ewmaChart.getChart(), pewmaChart.getChart()));
        metricSource.start();
    }
}
