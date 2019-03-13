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

import com.expedia.adaptivealerting.anomdetect.pewma.PewmaAnomalyDetector;
import com.expedia.adaptivealerting.anomdetect.pewma.PewmaParams;
import com.expedia.adaptivealerting.core.data.MetricFrame;
import com.expedia.adaptivealerting.core.data.MetricFrameLoader;
import com.expedia.adaptivealerting.core.evaluator.RmseEvaluator;
import com.expedia.adaptivealerting.tools.pipeline.filter.AnomalyDetectorFilter;
import com.expedia.adaptivealerting.tools.pipeline.filter.EvaluatorFilter;
import com.expedia.adaptivealerting.tools.pipeline.source.MetricFrameMetricSource;
import com.expedia.adaptivealerting.tools.pipeline.util.PipelineFactory;
import com.expedia.metrics.MetricDefinition;
import lombok.val;

import java.io.InputStream;
import java.util.UUID;

import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.createChartFrame;
import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.showChartFrame;

public class CsvTrafficPewmaVariants {

    public static void main(String[] args) throws Exception {

        // TODO Use the FileDataConnector rather than the MetricFrameLoader. [WLW]
        final InputStream is = ClassLoader.getSystemResourceAsStream("samples/cal-inflow.csv");
        final MetricFrame frame = MetricFrameLoader.loadCsv(new MetricDefinition("csv"), is, true);
        final MetricFrameMetricSource source = new MetricFrameMetricSource(frame, "data", 200L);

        val params1 = new PewmaParams()
                .setAlpha(0.15)
                .setBeta(1.0)
                .setWeakSigmas(2.0)
                .setStrongSigmas(3.0)
                .setInitMeanEstimate(0.0);
        val detector1 = new PewmaAnomalyDetector();
        detector1.init(UUID.randomUUID(), params1);
        val detectorFilter1 = new AnomalyDetectorFilter(detector1);

        val params2 = new PewmaParams()
                .setAlpha(0.25)
                .setBeta(1.0)
                .setWeakSigmas(2.0)
                .setStrongSigmas(3.0)
                .setInitMeanEstimate(0.0);
        val detector2 = new PewmaAnomalyDetector();
        val detectorFilter2 = new AnomalyDetectorFilter(detector2);

        val params3 = new PewmaParams()
                .setAlpha(0.35)
                .setBeta(1.0)
                .setWeakSigmas(2.0)
                .setStrongSigmas(3.0)
                .setInitMeanEstimate(0.0);
        val detector3 = new PewmaAnomalyDetector();
        val detectorFilter3 = new AnomalyDetectorFilter(detector3);

        val eval1 = new EvaluatorFilter(new RmseEvaluator());
        val eval2 = new EvaluatorFilter(new RmseEvaluator());
        val eval3 = new EvaluatorFilter(new RmseEvaluator());

        val chart1 = PipelineFactory.createChartSink("PEWMA: alpha=0.15");
        val chart2 = PipelineFactory.createChartSink("PEWMA: alpha=0.25");
        val chart3 = PipelineFactory.createChartSink("PEWMA: alpha=0.35");

        source.addSubscriber(detectorFilter1);
        source.addSubscriber(detectorFilter2);
        source.addSubscriber(detectorFilter3);

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
        source.start();
    }
}
