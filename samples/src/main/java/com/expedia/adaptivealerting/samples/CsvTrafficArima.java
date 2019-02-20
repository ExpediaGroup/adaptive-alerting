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

/* This package, Java Class CSVTrafficArima.java, uses https://github.com/Workday/timeseries-forecast Java open source library.
 * Adding the copyright notice and permission notice as required by the library's license.

 * Copyright 2017 Workday, Inc.

 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

import com.expedia.adaptivealerting.anomdetect.arima.ArimaAnomalyDetector;
import com.expedia.adaptivealerting.core.data.MetricFrame;
import com.expedia.adaptivealerting.core.data.io.MetricFrameLoader;
import com.expedia.adaptivealerting.core.evaluator.RmseEvaluator;
import com.expedia.adaptivealerting.tools.pipeline.filter.AnomalyDetectorFilter;
import com.expedia.adaptivealerting.tools.pipeline.filter.EvaluatorFilter;
import com.expedia.adaptivealerting.tools.pipeline.sink.AnomalyChartSink;
import com.expedia.adaptivealerting.tools.pipeline.source.MetricFrameMetricSource;
import com.expedia.adaptivealerting.tools.pipeline.util.PipelineFactory;
import com.expedia.metrics.MetricDefinition;

import java.io.InputStream;

import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.createChartFrame;
import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.showChartFrame;

/**
 * @author ddivakar
 */
public final class CsvTrafficArima {

    public static void main(String[] args) throws Exception {

        // TODO Use the FileDataConnector rather than the MetricFrameLoader. [WLW]
        /* The arima model works best with data that appear to have a stationary mean and variance after an order of differencing the series,
         *  here suitable-dataset.csv is a placeholder name to be replaced by applicable data.
         */
        final InputStream is = ClassLoader.getSystemResourceAsStream("samples/suitable-dataset.csv");
        final MetricFrame frame = MetricFrameLoader.loadCsv(new MetricDefinition("csv"), is, true);
        final MetricFrameMetricSource source = new MetricFrameMetricSource(frame, "data", 200L);

        final AnomalyDetectorFilter detectorFilter = new AnomalyDetectorFilter(new ArimaAnomalyDetector());
        final EvaluatorFilter evaluator = new EvaluatorFilter(new RmseEvaluator());
        final AnomalyChartSink chartWrapper = PipelineFactory.createChartSink("ArimaChart");

        source.addSubscriber(detectorFilter);
        detectorFilter.addSubscriber(evaluator);
        detectorFilter.addSubscriber(chartWrapper);
        evaluator.addSubscriber(chartWrapper);

        showChartFrame(createChartFrame("Cal Inflow", chartWrapper.getChart()));
        source.start();
    }
}
