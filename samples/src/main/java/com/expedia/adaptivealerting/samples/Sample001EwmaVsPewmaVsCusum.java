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
import com.expedia.adaptivealerting.anomdetect.detect.algo.CusumOutlierDetector;
import com.expedia.adaptivealerting.anomdetect.detect.algo.CusumOutlierDetectorParams;
import com.expedia.adaptivealerting.anomdetect.forecast.RmsePointForecastEvaluator;
import com.expedia.adaptivealerting.samples.util.DetectorUtil;
import com.expedia.adaptivealerting.tools.pipeline.filter.DetectorFilter;
import com.expedia.adaptivealerting.tools.pipeline.filter.EvaluatorFilter;
import com.expedia.adaptivealerting.tools.pipeline.util.PipelineFactory;
import lombok.val;

import java.util.UUID;

import static com.expedia.adaptivealerting.samples.util.MetricUtil.buildMetricFrameMetricSource;
import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.createChartFrame;
import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.showChartFrame;

public final class Sample001EwmaVsPewmaVsCusum {

    public static void main(String[] args) throws Exception {
        val source = buildMetricFrameMetricSource("samples/sample001.csv", 200L);

        val ewmaDetector = DetectorUtil.buildEwmaDetector();
        val ewmaFilter = new DetectorFilter(ewmaDetector);
        val ewmaEval = new EvaluatorFilter(new RmsePointForecastEvaluator());
        val ewmaChart = PipelineFactory.createChartSink("EWMA");

        source.addSubscriber(ewmaFilter);
        ewmaFilter.addSubscriber(ewmaEval);
        ewmaFilter.addSubscriber(ewmaChart);
        ewmaEval.addSubscriber(ewmaChart);

        val pewmaDetector = DetectorUtil.buildPewmaDetector();
        val pewmaFilter = new DetectorFilter(pewmaDetector);
        val pewmaEval = new EvaluatorFilter(new RmsePointForecastEvaluator());
        val pewmaChart = PipelineFactory.createChartSink("PEWMA");

        source.addSubscriber(pewmaFilter);
        pewmaFilter.addSubscriber(pewmaEval);
        pewmaFilter.addSubscriber(pewmaChart);
        pewmaEval.addSubscriber(pewmaChart);

        val cusumParams = new CusumOutlierDetectorParams()
                .setType(AnomalyType.RIGHT_TAILED)
                .setTargetValue(20_000_000)
                .setWeakSigmas(3.0)
                .setStrongSigmas(4.0)
                .setInitMeanEstimate(13_000_000);
        val cusumDetector = new CusumOutlierDetector(UUID.randomUUID(), cusumParams);
        val cusumFilter = new DetectorFilter(cusumDetector);
        val cusumChart = PipelineFactory.createChartSink("CUSUM");
        source.addSubscriber(cusumFilter);
        cusumFilter.addSubscriber(cusumChart);

        showChartFrame(createChartFrame(
                "Sample001.csv",
                ewmaChart.getChart(),
                pewmaChart.getChart(),
                cusumChart.getChart()));
        source.start();
    }
}
