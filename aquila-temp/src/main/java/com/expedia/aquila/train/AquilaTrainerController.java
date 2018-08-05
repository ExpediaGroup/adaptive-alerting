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
package com.expedia.aquila.train;

import com.expedia.adaptivealerting.core.data.Metric;
import com.expedia.adaptivealerting.core.data.MetricFrame;
import com.expedia.aquila.detect.AquilaAnomalyDetector;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * @author Willie Wheeler
 * @author Karan Shah
 */
@RestController
public class AquilaTrainerController {
    private static final Logger log = LoggerFactory.getLogger(AquilaTrainerController.class);
    
    private static final String CONFIG_BASE_RESOURCE_NAME = "trainer/application";
    
    private TrainerContext trainerContext;
    private AquilaTrainer trainer;
    
    // FIXME Switch to dependency injection
    public AquilaTrainerController() {
        final Config appConfig = ConfigFactory.load(CONFIG_BASE_RESOURCE_NAME);
        this.trainerContext = new TrainerContext(appConfig);
        this.trainer = new AquilaTrainer(trainerContext);
    }
    
    @RequestMapping("/train")
    public String train() {
        log.trace("Training model");
        
        // FIXME Replace hardcoded params [WLW]
        final Metric metric = dummyMetric();
        final TrainingParams params = new TrainingParams();
        final TrainingTask task = new TrainingTask(metric, params);
        
        final Instant startDate = Instant.parse("2018-04-29T00:00:00Z");
        final Instant endDate = Instant.parse("2018-07-22T00:00:00Z");
        final MetricFrame data = trainerContext.dataConnector().load(metric, startDate, endDate);
        
        final AquilaAnomalyDetector detector = trainer.train(task, data);
        trainerContext.aquilaAnomalyDetectorRepo().save(detector);
        
        return detector.getUuid().toString();
    }
    
    private Metric dummyMetric() {
        final Metric metric = new Metric();
        metric.putTag("mtype", "count");
        metric.putTag("unit", "");
        metric.putTag("what", "bookings");
        metric.putTag("lob", "hotels");
        metric.putTag("pos", "expedia-com");
        metric.putTag("interval", "5m");
        return metric;
    }
}
