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

import com.expedia.adaptivealerting.anomdetect.cusum.CusumAnomalyDetector;
import com.expedia.adaptivealerting.anomdetect.cusum.CusumParams;
import com.expedia.adaptivealerting.anomdetect.ewma.EwmaAnomalyDetector;
import com.expedia.adaptivealerting.anomdetect.ewma.EwmaParams;
import com.expedia.adaptivealerting.anomdetect.pewma.PewmaAnomalyDetector;
import com.expedia.adaptivealerting.anomdetect.pewma.PewmaParams;
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
 * @author Willie Wheeler
 */
public final class Sample001 {
    
    public static void main(String[] args) throws Exception {
        
        // TODO Use the FileDataConnector rather than the MetricFrameLoader. [WLW]
        final InputStream is = ClassLoader.getSystemResourceAsStream("samples/sample001.csv");
        final MetricFrame frame = MetricFrameLoader.loadCsv(new MetricDefinition("csv"), is, true);
        final MetricFrameMetricSource source = new MetricFrameMetricSource(frame, "data", 200L);
        
        final EwmaParams ewmaParams = new EwmaParams()
                .setAlpha(0.20)
                .setWeakSigmas(4.5)
                .setStrongSigmas(5.5);
        final EwmaAnomalyDetector ewmaAD = new EwmaAnomalyDetector(ewmaParams);
        final AnomalyDetectorFilter ewmaADF = new AnomalyDetectorFilter(ewmaAD);
    
        final PewmaParams pewmaParams = new PewmaParams()
                .setAlpha(0.20)
                .setWeakSigmas(5.0)
                .setStrongSigmas(6.0);
        final PewmaAnomalyDetector pewmaAD = new PewmaAnomalyDetector(pewmaParams);
        final AnomalyDetectorFilter pewmaADF = new AnomalyDetectorFilter(pewmaAD);
        
        final CusumParams cusumParams = new CusumParams()
                .setType(CusumParams.Type.RIGHT_TAILED)
                .setTargetValue(20_000_000)
                .setWeakSigmas(3.0)
                .setStrongSigmas(4.0)
                .setInitMeanEstimate(13_000_000);
        final CusumAnomalyDetector cusumAD = new CusumAnomalyDetector(cusumParams);
        final AnomalyDetectorFilter cusumADF = new AnomalyDetectorFilter(cusumAD);
        
        final EvaluatorFilter ewmaEval = new EvaluatorFilter(new RmseEvaluator());
        final EvaluatorFilter pewmaEval = new EvaluatorFilter(new RmseEvaluator());
        
        final AnomalyChartSink ewmaChart = PipelineFactory.createChartSink("EWMA");
        final AnomalyChartSink pewmaChart = PipelineFactory.createChartSink("PEWMA");
        final AnomalyChartSink cusumChart = PipelineFactory.createChartSink("CUSUM");
        
        source.addSubscriber(ewmaADF);
        ewmaADF.addSubscriber(ewmaEval);
        ewmaADF.addSubscriber(ewmaChart);
        ewmaEval.addSubscriber(ewmaChart);
        
        source.addSubscriber(pewmaADF);
        pewmaADF.addSubscriber(pewmaEval);
        pewmaADF.addSubscriber(pewmaChart);
        pewmaEval.addSubscriber(pewmaChart);
        
        source.addSubscriber(cusumADF);
        cusumADF.addSubscriber(cusumChart);
        
        showChartFrame(createChartFrame(
                "Sample001.csv",
                ewmaChart.getChart(),
                pewmaChart.getChart(),
                cusumChart.getChart()));
        source.start();
    }
}
