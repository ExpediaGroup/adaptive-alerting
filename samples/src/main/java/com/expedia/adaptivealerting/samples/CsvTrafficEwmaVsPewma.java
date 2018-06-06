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
import com.expedia.adaptivealerting.tools.pipeline.filter.AnomalyDetectorStreamFilter;
import com.expedia.adaptivealerting.tools.pipeline.sink.AnomalyChartStreamSink;
import com.expedia.adaptivealerting.tools.pipeline.sink.ConsoleLogStreamSink;
import com.expedia.adaptivealerting.tools.pipeline.source.CsvMetricSource;
import com.expedia.adaptivealerting.tools.visualization.ChartSeries;

import java.io.InputStream;

import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.*;

/**
 * @author Willie Wheeler
 */
public class CsvTrafficEwmaVsPewma {
    
    public static void main(String[] args) {
        final InputStream is = ClassLoader.getSystemResourceAsStream("samples/sample001.csv");
        final CsvMetricSource source = new CsvMetricSource(is, "data", 1000L);
        
        final AnomalyDetectorStreamFilter ewmaFilter = new AnomalyDetectorStreamFilter(new EwmaAnomalyDetector());
        final AnomalyDetectorStreamFilter pewmaFilter = new AnomalyDetectorStreamFilter(new PewmaAnomalyDetector());
        
        final ChartSeries ewmaSeries = new ChartSeries();
        final ChartSeries pewmaSeries = new ChartSeries();
    
        source.addMetricPointSubscriber(ewmaFilter);
        source.addMetricPointSubscriber(pewmaFilter);
        ewmaFilter.addAnomalyResultSubscriber(new ConsoleLogStreamSink());
        ewmaFilter.addAnomalyResultSubscriber(new AnomalyChartStreamSink(ewmaSeries));
        pewmaFilter.addAnomalyResultSubscriber(new ConsoleLogStreamSink());
        pewmaFilter.addAnomalyResultSubscriber(new AnomalyChartStreamSink(pewmaSeries));
        
        showChartFrame(createChartFrame(
                "Cal Inflow",
                createChart("EWMA", ewmaSeries),
                createChart("PEWMA", pewmaSeries)));
    
        source.start();
    }
}
