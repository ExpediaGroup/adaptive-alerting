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

import com.expedia.adaptivealerting.anomdetect.comp.legacy.DetectorLookup;
import com.expedia.adaptivealerting.anomdetect.comp.legacy.EwmaParams;
import com.expedia.adaptivealerting.anomdetect.comp.legacy.LegacyDetectorFactory;
import com.expedia.adaptivealerting.anomdetect.comp.legacy.PewmaDetector;
import com.expedia.adaptivealerting.anomdetect.comp.legacy.PewmaParams;
import com.expedia.adaptivealerting.anomdetect.detector.CusumDetector;
import com.expedia.adaptivealerting.anomdetect.detector.CusumParams;
import com.expedia.adaptivealerting.core.anomaly.AnomalyType;
import com.expedia.adaptivealerting.core.evaluator.RmseEvaluator;
import com.expedia.adaptivealerting.tools.pipeline.filter.DetectorFilter;
import com.expedia.adaptivealerting.tools.pipeline.filter.EvaluatorFilter;
import com.expedia.adaptivealerting.tools.pipeline.util.PipelineFactory;
import lombok.val;

import java.util.UUID;

import static com.expedia.adaptivealerting.samples.MetricGenerationHelper.buildMetricFrameMetricSource;
import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.createChartFrame;
import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.showChartFrame;

public final class Sample001 {

    public static void main(String[] args) throws Exception {
        val source = buildMetricFrameMetricSource("samples/sample001.csv", 200L);

        val factory = new LegacyDetectorFactory(new DetectorLookup());
        val ewmaParams = new EwmaParams()
                .setAlpha(0.20)
                .setWeakSigmas(4.5)
                .setStrongSigmas(5.5);
        val ewmaAD = factory.createEwmaDetector(UUID.randomUUID(), ewmaParams);
        val ewmaADF = new DetectorFilter(ewmaAD);

        val pewmaParams = new PewmaParams()
                .setAlpha(0.20)
                .setWeakSigmas(5.0)
                .setStrongSigmas(6.0);
        val pewmaAD = new PewmaDetector();
        pewmaAD.init(UUID.randomUUID(), pewmaParams, AnomalyType.TWO_TAILED);
        val pewmaADF = new DetectorFilter(pewmaAD);

        val cusumParams = new CusumParams()
                .setType(AnomalyType.RIGHT_TAILED)
                .setTargetValue(20_000_000)
                .setWeakSigmas(3.0)
                .setStrongSigmas(4.0)
                .setInitMeanEstimate(13_000_000);
        val cusumAD = new CusumDetector();
        cusumAD.init(UUID.randomUUID(), cusumParams, AnomalyType.TWO_TAILED);
        val cusumADF = new DetectorFilter(cusumAD);

        val ewmaEval = new EvaluatorFilter(new RmseEvaluator());
        val pewmaEval = new EvaluatorFilter(new RmseEvaluator());

        val ewmaChart = PipelineFactory.createChartSink("EWMA");
        val pewmaChart = PipelineFactory.createChartSink("PEWMA");
        val cusumChart = PipelineFactory.createChartSink("CUSUM");

        source.addSubscriber(ewmaADF);
        ewmaADF.addSubscriber(ewmaEval);
        ewmaADF.addSubscriber(ewmaChart);
        ewmaEval.addSubscriber(ewmaChart);

        source.addSubscriber(pewmaADF);
        pewmaADF.addSubscriber(pewmaEval);
        pewmaADF.addSubscriber(pewmaChart);
        pewmaEval.addSubscriber(pewmaChart);

        source.addSubscriber(cusumADF);
        cusumADF.addSubscriber(cusumChart);

        showChartFrame(createChartFrame(
                "Sample001.csv",
                ewmaChart.getChart(),
                pewmaChart.getChart(),
                cusumChart.getChart()));
        source.start();
    }
}
