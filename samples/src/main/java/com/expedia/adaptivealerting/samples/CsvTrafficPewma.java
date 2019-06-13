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

import com.expedia.adaptivealerting.anomdetect.comp.legacy.LegacyDetectorFactory;
import com.expedia.adaptivealerting.anomdetect.comp.legacy.PewmaParams;
import com.expedia.adaptivealerting.anomdetect.util.MetricFrameLoader;
import com.expedia.adaptivealerting.anomdetect.forecast.evaluate.RmseEvaluator;
import com.expedia.adaptivealerting.tools.pipeline.filter.DetectorFilter;
import com.expedia.adaptivealerting.tools.pipeline.filter.EvaluatorFilter;
import com.expedia.adaptivealerting.tools.pipeline.source.MetricFrameMetricSource;
import com.expedia.adaptivealerting.tools.pipeline.util.PipelineFactory;
import com.expedia.metrics.MetricDefinition;
import lombok.val;

import java.util.UUID;

import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.createChartFrame;
import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.showChartFrame;

public final class CsvTrafficPewma {

    public static void main(String[] args) throws Exception {

        // TODO Use the FileDataConnector rather than the MetricFrameLoader. [WLW]
        val is = ClassLoader.getSystemResourceAsStream("samples/cal-inflow.csv");
        val frame = MetricFrameLoader.loadCsv(new MetricDefinition("csv"), is, true);
        val source = new MetricFrameMetricSource(frame, "data", 200L);

        val factory = new LegacyDetectorFactory();

        val detector = factory.createPewmaDetector(UUID.randomUUID(), new PewmaParams());
        val filter = new DetectorFilter(detector);
        val evaluator = new EvaluatorFilter(new RmseEvaluator());
        val chartWrapper = PipelineFactory.createChartSink("PEWMA");

        source.addSubscriber(filter);
        filter.addSubscriber(evaluator);
        filter.addSubscriber(chartWrapper);
        evaluator.addSubscriber(chartWrapper);

        showChartFrame(createChartFrame("Cal Inflow", chartWrapper.getChart()));
        source.start();
    }
}
