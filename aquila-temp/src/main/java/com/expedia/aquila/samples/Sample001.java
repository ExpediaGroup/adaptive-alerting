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
package com.expedia.aquila.samples;

import com.expedia.adaptivealerting.anomdetect.EwmaAnomalyDetector;
import com.expedia.adaptivealerting.anomdetect.PewmaAnomalyDetector;
import com.expedia.adaptivealerting.core.data.Metric;
import com.expedia.adaptivealerting.core.data.MetricFrame;
import com.expedia.adaptivealerting.core.evaluator.RmseEvaluator;
import com.expedia.adaptivealerting.tools.pipeline.filter.AnomalyDetectorFilter;
import com.expedia.adaptivealerting.tools.pipeline.filter.EvaluatorFilter;
import com.expedia.adaptivealerting.tools.pipeline.sink.AnomalyChartSink;
import com.expedia.adaptivealerting.tools.pipeline.source.MetricFrameMetricSource;
import com.expedia.adaptivealerting.tools.pipeline.source.MetricSource;
import com.expedia.adaptivealerting.tools.pipeline.util.PipelineFactory;
import com.expedia.aquila.AppConfigFactory;
import com.expedia.aquila.AppContext;
import com.expedia.aquila.AquilaAnomalyDetector;
import com.expedia.aquila.model.DecompType;
import com.expedia.aquila.train.AquilaTrainer;
import com.expedia.aquila.train.TrainingParams;
import com.expedia.aquila.train.TrainingTask;
import com.typesafe.config.Config;
import org.jfree.ui.ApplicationFrame;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;
import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.createChartFrame;
import static com.expedia.adaptivealerting.tools.visualization.ChartUtil.showChartFrame;

/**
 * Sample pipeline 001.
 *
 * @author Willie Wheeler
 */
public final class Sample001 {
    private static final int TICK_SIZE = 5;
    private static final int TICKS_PER_WEEK = 7 * 24 * 60 / TICK_SIZE;
    
    private AppContext appContext;
    private MetricSource source;
    private ApplicationFrame chartFrame;
    
    public static void main(String[] args) {
        final Config appConfig = AppConfigFactory.create();
        final AppContext appContext = new AppContext(appConfig);
        new Sample001(appContext).start();
    }
    
    public Sample001(AppContext appContext) {
        notNull(appContext, "appContext can't be null");
        
        this.appContext = appContext;
        
        // TODO
        final Metric metric = new Metric();
        final MetricFrame trainingData = appContext.metricDataRepo().load(metric, "sample001-train.csv");
        final MetricFrame streamData = appContext.metricDataRepo().load(metric, "sample001-test.csv");
        
        // Train Aquila model
        final TrainingParams params = new TrainingParams()
                .tickSize(TICK_SIZE)
                .decompType(DecompType.MULTIPLICATIVE)
                .periodSize(TICKS_PER_WEEK)
                .wmaWindowSize(17);
        
        final TrainingTask task = new TrainingTask(metric, params);
        final AquilaAnomalyDetector aquilaAD = new AquilaTrainer(appContext).train(task, trainingData);
        
        this.source = new MetricFrameMetricSource(streamData, "sample001", 50L);
        
        final AnomalyDetectorFilter ewmaADF = new AnomalyDetectorFilter(new EwmaAnomalyDetector());
        final AnomalyDetectorFilter pewmaADF = new AnomalyDetectorFilter(new PewmaAnomalyDetector());
        final AnomalyDetectorFilter aquilaADF = new AnomalyDetectorFilter(aquilaAD);
        
        final EvaluatorFilter ewmaEF = new EvaluatorFilter(new RmseEvaluator());
        final EvaluatorFilter pewmaEF = new EvaluatorFilter(new RmseEvaluator());
        final EvaluatorFilter aquilaEF = new EvaluatorFilter(new RmseEvaluator());
    
        final AnomalyChartSink ewmaACS = PipelineFactory.createChartSink("EWMA");
        final AnomalyChartSink pewmaACS = PipelineFactory.createChartSink("PEWMA");
        final AnomalyChartSink aquilaACS = PipelineFactory.createChartSink("Aquila");
    
        source.addSubscriber(ewmaADF);
        ewmaADF.addSubscriber(ewmaEF);
        ewmaADF.addSubscriber(ewmaACS);
        ewmaEF.addSubscriber(ewmaACS);
    
        source.addSubscriber(pewmaADF);
        pewmaADF.addSubscriber(pewmaEF);
        pewmaADF.addSubscriber(pewmaACS);
        pewmaEF.addSubscriber(pewmaACS);
        
        source.addSubscriber(aquilaADF);
        aquilaADF.addSubscriber(aquilaEF);
        aquilaADF.addSubscriber(aquilaACS);
        aquilaEF.addSubscriber(aquilaACS);
        
        this.chartFrame = createChartFrame(
                "Aquila v EWMA",
                ewmaACS.getChart(),
//                pewmaACS.getChart(),
                aquilaACS.getChart());
    }
    
    public void start() {
        showChartFrame(chartFrame);
        source.start();
    }
}
