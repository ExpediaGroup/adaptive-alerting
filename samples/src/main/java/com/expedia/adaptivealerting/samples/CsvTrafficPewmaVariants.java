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
public class CsvTrafficPewmaVariants {
    
    public static void main(String[] args) {
        final InputStream is = ClassLoader.getSystemResourceAsStream("samples/sample001.csv");
        final CsvMetricSource source = new CsvMetricSource(is, "data", 1000L);
        
        final AnomalyDetectorFilter filter1 =
                new AnomalyDetectorFilter(new PewmaAnomalyDetector(0.15, 1.0, 2.0, 3.0, 0.0));
        final AnomalyDetectorFilter filter2 =
                new AnomalyDetectorFilter(new PewmaAnomalyDetector(0.25, 1.0, 2.0, 3.0, 0.0));
        final AnomalyDetectorFilter filter3 =
                new AnomalyDetectorFilter(new PewmaAnomalyDetector(0.35, 1.0, 2.0, 3.0, 0.0));
        
        final ChartSeries series1 = new ChartSeries();
        final ChartSeries series2 = new ChartSeries();
        final ChartSeries series3 = new ChartSeries();
    
        final JFreeChart chart1 = createChart("PEWMA: alpha=0.15", series1);
        final JFreeChart chart2 = createChart("PEWMA: alpha=0.25", series2);
        final JFreeChart chart3 = createChart("PEWMA: alpha=0.35", series3);
    
        final AnomalyChartSink sink1 = new AnomalyChartSink(chart1, series1);
        final AnomalyChartSink sink2 = new AnomalyChartSink(chart2, series2);
        final AnomalyChartSink sink3 = new AnomalyChartSink(chart3, series3);
        
        source.addSubscriber(filter1);
        source.addSubscriber(filter2);
        source.addSubscriber(filter3);
        
        filter1.addSubscriber(sink1);
        filter2.addSubscriber(sink2);
        filter3.addSubscriber(sink3);
        
        showChartFrame(createChartFrame("Cal Inflow", chart1, chart2, chart3));
        source.start();
    }
}
