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

import com.expedia.adaptivealerting.anomdetect.comp.legacy.EwmaParams;
import com.expedia.adaptivealerting.anomdetect.comp.legacy.LegacyDetectorFactory;
import com.expedia.adaptivealerting.anomdetect.comp.legacy.PewmaParams;
import com.expedia.adaptivealerting.anomdetect.forecast.evaluate.RmseEvaluator;
import com.expedia.adaptivealerting.tools.pipeline.filter.DetectorFilter;
import com.expedia.adaptivealerting.tools.pipeline.filter.EvaluatorFilter;
import com.expedia.adaptivealerting.tools.pipeline.source.RandomWalkMetricSource;
import com.expedia.adaptivealerting.tools.pipeline.util.PipelineFactory;
import lombok.val;

import java.util.UUID;

import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.createChartFrame;
import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.showChartFrame;

/**
 * Sample pipeline based on a random walk with EWMA and PEWMA filters.
 */
public class RandomWalkEwmaVsPewma {

    public static void main(String[] args) {
        val source = new RandomWalkMetricSource();
        val factory = new LegacyDetectorFactory();

        val ewmaDetector = factory.createEwmaDetector(UUID.randomUUID(), new EwmaParams());
        val ewmaFilter = new DetectorFilter(ewmaDetector);
        val ewmaEval = new EvaluatorFilter(new RmseEvaluator());
        val ewmaChart = PipelineFactory.createChartSink("EWMA");
        source.addSubscriber(ewmaFilter);
        ewmaFilter.addSubscriber(ewmaEval);
        ewmaFilter.addSubscriber(ewmaChart);
        ewmaEval.addSubscriber(ewmaChart);

        val pewmaDetector = factory.createPewmaDetector(UUID.randomUUID(), new PewmaParams());
        val pewmaFilter = new DetectorFilter(pewmaDetector);
        val pewmaEval = new EvaluatorFilter(new RmseEvaluator());
        val pewmaChart = PipelineFactory.createChartSink("PEWMA");
        source.addSubscriber(pewmaFilter);
        pewmaFilter.addSubscriber(pewmaEval);
        pewmaFilter.addSubscriber(pewmaChart);
        pewmaEval.addSubscriber(pewmaChart);

        showChartFrame(createChartFrame("Random Walk", ewmaChart.getChart(), pewmaChart.getChart()));
        source.start();
    }
}
