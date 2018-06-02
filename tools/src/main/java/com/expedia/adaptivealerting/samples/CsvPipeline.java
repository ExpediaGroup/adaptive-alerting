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

import com.expedia.adaptivealerting.core.OutlierDetector;
import com.expedia.adaptivealerting.core.detector.PewmaOutlierDetector;
import com.expedia.adaptivealerting.tools.pipeline.MetricFilter;
import com.expedia.adaptivealerting.tools.pipeline.MetricSink;
import com.expedia.adaptivealerting.tools.pipeline.MetricSource;
import com.expedia.adaptivealerting.tools.pipeline.filter.OutlierDetectorMetricFilter;
import com.expedia.adaptivealerting.tools.pipeline.sink.ChartSink;
import com.expedia.adaptivealerting.tools.pipeline.sink.ConsoleLogMetricSink;
import com.expedia.adaptivealerting.tools.pipeline.source.CsvMetricSource;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;

import java.io.InputStream;

import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.*;

public class CsvPipeline {
    
    public static void main(String[] args) {
        final InputStream is = ClassLoader.getSystemResourceAsStream("samples/sample001.csv");
        final MetricSource source = new CsvMetricSource(is, "data", 200L);
    
        final OutlierDetector detector = new PewmaOutlierDetector(0.1, 1.0, 2.0, 3.0, 0.0);
        final MetricFilter filter = new OutlierDetectorMetricFilter(detector);
        source.addSubscriber(filter);
    
        final MetricSink consoleSink = new ConsoleLogMetricSink();
        filter.addSubscriber(consoleSink);
    
        final TimeSeries observed = new TimeSeries("observed");
        final TimeSeries predictedUpper = new TimeSeries("predictedUpper");
        final TimeSeries predictedLower = new TimeSeries("predictedLower");
    
        final MetricSink chartSink = new ChartSink(predictedUpper, predictedLower, observed);
        filter.addSubscriber(chartSink);
    
        final TimeSeriesCollection band = new TimeSeriesCollection();
        band.addSeries(predictedUpper);
        band.addSeries(predictedLower);
    
        final JFreeChart chart = createChart("Cal Inflow", band, new TimeSeriesCollection(observed));
        final ApplicationFrame chartFrame = createChartFrame(chart);
        showChartFrame(chartFrame);
    
        source.start();
    }
}
