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
import com.expedia.adaptivealerting.tools.pipeline.source.CsvMetricSource;
import com.expedia.adaptivealerting.tools.visualization.ChartSeries;
import org.jfree.chart.JFreeChart;

import java.io.InputStream;

import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.*;

/**
 * @author Willie Wheeler
 */
public class CsvTrafficEwmaVsPewma {
    
    public static void main(String[] args) {
        final InputStream is = ClassLoader.getSystemResourceAsStream("samples/sample001.csv");
        final CsvMetricSource source = new CsvMetricSource(is, "data", 1000L);
        
        final AnomalyDetectorFilter ewmaFilter = new AnomalyDetectorFilter(new EwmaAnomalyDetector());
        final AnomalyDetectorFilter pewmaFilter = new AnomalyDetectorFilter(new PewmaAnomalyDetector());
        
        final ChartSeries ewmaSeries = new ChartSeries();
        final ChartSeries pewmaSeries = new ChartSeries();
        
        final JFreeChart ewmaChart = createChart("EWMA", ewmaSeries);
        final JFreeChart pewmaChart = createChart("PEWMA", pewmaSeries);
    
        final AnomalyChartSink ewmaSink = new AnomalyChartSink(ewmaChart, ewmaSeries);
        final AnomalyChartSink pewmaSink = new AnomalyChartSink(pewmaChart, pewmaSeries);
    
        source.addSubscriber(ewmaFilter);
        source.addSubscriber(pewmaFilter);
        ewmaFilter.addSubscriber(ewmaSink);
        pewmaFilter.addSubscriber(pewmaSink);
        
        showChartFrame(createChartFrame("Cal Inflow", ewmaChart, pewmaChart));
        source.start();
    }
}
