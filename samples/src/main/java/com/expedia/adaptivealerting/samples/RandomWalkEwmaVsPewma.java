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

import com.expedia.adaptivealerting.anomdetect.EwmaAnomalyDetector;
import com.expedia.adaptivealerting.anomdetect.PewmaAnomalyDetector;
import com.expedia.adaptivealerting.tools.pipeline.filter.AnomalyDetectorFilter;
import com.expedia.adaptivealerting.tools.pipeline.sink.AnomalyChartSink;
import com.expedia.adaptivealerting.tools.pipeline.source.RandomWalkMetricSource;
import com.expedia.adaptivealerting.tools.pipeline.util.PipelineFactory;

import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.createChartFrame;
import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.showChartFrame;

/**
 * Sample pipeline based on a random walk with EWMA and PEWMA filters.
 *
 * @author Willie Wheeler
 */
public class RandomWalkEwmaVsPewma {
    
    public static void main(String[] args) {
        final RandomWalkMetricSource source = new RandomWalkMetricSource();
        
        final AnomalyDetectorFilter ewmaFilter = new AnomalyDetectorFilter(new EwmaAnomalyDetector());
        final AnomalyDetectorFilter pewmaFilter = new AnomalyDetectorFilter(new PewmaAnomalyDetector());
        
        final AnomalyChartSink ewmaChart = PipelineFactory.createChartSink("EWMA");
        final AnomalyChartSink pewmaChart = PipelineFactory.createChartSink("PEWMA");
        
        source.addSubscriber(ewmaFilter);
        source.addSubscriber(pewmaFilter);
        
        ewmaFilter.addSubscriber(ewmaChart);
        pewmaFilter.addSubscriber(pewmaChart);
        
        showChartFrame(createChartFrame("Random Walk", ewmaChart.getChart(), pewmaChart.getChart()));
        source.start();
    }
}
