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

import com.expedia.adaptivealerting.anomdetect.forecast.RmsePointForecastEvaluator;
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

public final class CsvTrafficEwma {

    public static void main(String[] args) throws Exception {
        val is = ClassLoader.getSystemResourceAsStream("samples/cal-inflow.csv");
        val metricFrame = MetricFrameLoader.loadCsv(new MetricDefinition("csv"), is, true);
        val metricSource = new MetricFrameMetricSource(metricFrame, "data", 200L);

        val detector = DetectorUtil.buildEwmaDetector();
        val detectorFilter = new DetectorFilter(detector);
        val evaluator = new EvaluatorFilter(new RmsePointForecastEvaluator());
        val chartWrapper = PipelineFactory.createChartSink("EWMA");

        metricSource.addSubscriber(detectorFilter);
        detectorFilter.addSubscriber(evaluator);
        detectorFilter.addSubscriber(chartWrapper);
        evaluator.addSubscriber(chartWrapper);

        showChartFrame(createChartFrame("Cal Inflow", chartWrapper.getChart()));
        metricSource.start();
    }
}
