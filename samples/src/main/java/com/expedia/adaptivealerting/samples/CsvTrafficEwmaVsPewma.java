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
import com.expedia.adaptivealerting.tools.pipeline.filter.OutlierDetectorStreamFilter;
import com.expedia.adaptivealerting.tools.pipeline.sink.OutlierChartStreamSink;
import com.expedia.adaptivealerting.tools.pipeline.sink.ConsoleLogStreamSink;
import com.expedia.adaptivealerting.tools.pipeline.source.CsvMetricSource;
import com.expedia.adaptivealerting.tools.visualization.ChartSeries;

import java.io.InputStream;

import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.*;

public class CsvTrafficEwmaVsPewma {
    
    public static void main(String[] args) {
        final InputStream is = ClassLoader.getSystemResourceAsStream("samples/sample001.csv");
        final CsvMetricSource source = new CsvMetricSource(is, "data", 1000L);
        
        final OutlierDetectorStreamFilter ewmaFilter = new OutlierDetectorStreamFilter(new EwmaAnomalyDetector());
        final OutlierDetectorStreamFilter pewmaFilter = new OutlierDetectorStreamFilter(new PewmaAnomalyDetector());
        
        final ChartSeries ewmaSeries = new ChartSeries();
        final ChartSeries pewmaSeries = new ChartSeries();
    
        source.addSubscriber(ewmaFilter);
        source.addSubscriber(pewmaFilter);
        ewmaFilter.addSubscriber(new ConsoleLogStreamSink());
        ewmaFilter.addSubscriber(new OutlierChartStreamSink(ewmaSeries));
        pewmaFilter.addSubscriber(new ConsoleLogStreamSink());
        pewmaFilter.addSubscriber(new OutlierChartStreamSink(pewmaSeries));
        
        showChartFrame(createChartFrame(
                "Cal Inflow",
                createChart("EWMA", ewmaSeries),
                createChart("PEWMA", pewmaSeries)));
    
        source.start();
    }
}
