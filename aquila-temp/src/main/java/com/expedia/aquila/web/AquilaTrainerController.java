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
package com.expedia.aquila.web;

import com.expedia.adaptivealerting.core.data.Metric;
import com.expedia.adaptivealerting.core.data.MetricFrame;
import com.expedia.aquila.AppContext;
import com.expedia.aquila.AquilaAnomalyDetector;
import com.expedia.aquila.train.AquilaTrainer;
import com.expedia.aquila.train.TrainingParams;
import com.expedia.aquila.train.TrainingTask;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Willie Wheeler
 */
@RestController
public class AquilaTrainerController {
    private static final Logger log = LoggerFactory.getLogger(AquilaTrainerController.class);
    
    private static final String CONFIG_PATH = "config/base.conf";
    private static final String TRAINING_DATA_PATH = "test-data/cal-inflow-train.csv";
    
    private AppContext appContext;
    private AquilaTrainer trainer;
    
    // FIXME Switch to dependency injection
    public AquilaTrainerController() {
        final Config appConfig = ConfigFactory.load(CONFIG_PATH);
        final Config aquilaConfig = appConfig.getConfig("aquila-detector");
        this.appContext = new AppContext(aquilaConfig);
        this.trainer = new AquilaTrainer(appContext);
    }
    
    @RequestMapping("/train")
    public String train() {
        log.trace("Training model");
        final Metric metric = dummyMetric();
        final TrainingParams params = new TrainingParams();
        final TrainingTask task = new TrainingTask(metric, params);
        final MetricFrame data = appContext.metricDataRepo().load(metric, TRAINING_DATA_PATH);
        final AquilaAnomalyDetector detector = trainer.train(task, data);
        appContext.aquilaAnomalyDetectorRepo().save(detector);
        return "train";
    }
    
    private Metric dummyMetric() {
        final Metric metric = new Metric();
        metric.putTag("what", "bookings");
        metric.putTag("lob", "hotels");
        metric.putTag("pos", "expedia.com");
        metric.putTag("mtype", "count");
        metric.putTag("unit", "");
        return metric;
    }
}
