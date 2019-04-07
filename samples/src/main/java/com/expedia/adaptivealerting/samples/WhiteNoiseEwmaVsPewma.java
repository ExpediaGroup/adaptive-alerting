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
import com.expedia.adaptivealerting.anomdetect.comp.legacy.LegacyDetectorFactory;
import com.expedia.adaptivealerting.tools.pipeline.filter.DetectorFilter;
import com.expedia.adaptivealerting.tools.pipeline.source.WhiteNoiseMetricSource;
import com.expedia.adaptivealerting.tools.pipeline.util.PipelineFactory;
import lombok.val;

import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.createChartFrame;
import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.showChartFrame;

/**
 * Sample pipeline based on white noise with EWMA and PEWMA filters.
 */
public class WhiteNoiseEwmaVsPewma {

    public static void main(String[] args) {
        val source = new WhiteNoiseMetricSource("white-noise", 1000L, 0.0, 1.0);

        val factory = new LegacyDetectorFactory(new DetectorLookup());
        val ewmaFilter = new DetectorFilter(factory.createEwmaDetector());
        val pewmaFilter = new DetectorFilter(factory.createPewmaDetector());

        val ewmaChart = PipelineFactory.createChartSink("EWMA");
        val pewmaChart = PipelineFactory.createChartSink("PEWMA");

        source.addSubscriber(ewmaFilter);
        source.addSubscriber(pewmaFilter);

        ewmaFilter.addSubscriber(ewmaChart);
        pewmaFilter.addSubscriber(pewmaChart);

        showChartFrame(createChartFrame("White Noise", ewmaChart.getChart(), pewmaChart.getChart()));
        source.start();
    }
}
