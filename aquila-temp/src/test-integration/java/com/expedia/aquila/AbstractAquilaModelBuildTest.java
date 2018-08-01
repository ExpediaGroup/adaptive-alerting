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
package com.expedia.aquila;

import com.expedia.adaptivealerting.core.data.MappedMpoint;
import com.expedia.adaptivealerting.core.data.Metric;
import com.expedia.adaptivealerting.core.data.MetricFrame;
import com.expedia.adaptivealerting.core.data.Mpoint;
import com.expedia.aquila.train.AquilaTrainer;
import com.expedia.aquila.train.TrainingParams;
import com.expedia.aquila.train.TrainingTask;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ListIterator;
import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * @author Willie Wheeler
 */
public abstract class AbstractAquilaModelBuildTest {
    private static final Logger log = LoggerFactory.getLogger(FileBasedAquilaModelBuildTest.class);
    
    private static final String TRAINING_DATA_PATH = "test-data/cal-inflow-train.csv";
    private static final String TEST_DATA_PATH = "test-data/cal-inflow-test.csv";
    
    private String configPath;
    private AppContext appContext;
    private Metric metric;
    
    public AbstractAquilaModelBuildTest(String configPath) {
        notNull(configPath, "configPath can't be null");
        this.configPath = configPath;
        final Config appConfig = ConfigFactory.load(configPath);
        final Config aquilaConfig = appConfig.getConfig("aquila-detector");
        this.appContext = new AppContext(aquilaConfig);
        
        this.metric = new Metric();
        metric.putTag("what", "cal-inflow");
        metric.putTag("mtype", "count");
        metric.putTag("unit", "");
    }
    
    @Test
    public void testModelBuild() {
        log.trace("Testing model build");
        final MetricFrame trainingData = loadTrainingData();
        final AquilaAnomalyDetector trainedModel = trainModel(trainingData);
        final UUID uuid = storeModel(trainedModel);
        
        final AquilaAnomalyDetector loadedModel = loadModel(uuid);
        final MetricFrame testData = loadTestData();
        processTestData(loadedModel, testData);
    }
    
    private MetricFrame loadTrainingData() {
        log.trace("Loading training data");
        final MetricFrame data = appContext.metricDataRepo().load(metric, TRAINING_DATA_PATH);
        log.trace("Loaded {} rows", data.getNumRows());
        return data;
    }
    
    private AquilaAnomalyDetector trainModel(MetricFrame data) {
        log.trace("Training model");
        final AquilaTrainer trainer = new AquilaTrainer(appContext);
        final TrainingParams params = new TrainingParams();
        final TrainingTask task = new TrainingTask(metric, params);
        return trainer.train(task, data);
    }
    
    private UUID storeModel(AquilaAnomalyDetector model) {
        log.trace("Storing model");
        appContext.aquilaAnomalyDetectorRepo().save(model);
        final UUID uuid = model.getUuid();
        log.trace("Stored model: uuid={}", uuid);
        return uuid;
    }
    
    private AquilaAnomalyDetector loadModel(UUID uuid) {
        log.trace("Loading model: uuid={}", uuid);
        return appContext.aquilaAnomalyDetectorRepo().load(uuid);
    }
    
    private MetricFrame loadTestData() {
        log.trace("Loading test data");
        final MetricFrame data = appContext.metricDataRepo().load(metric, TEST_DATA_PATH);
        log.trace("Loaded {} rows", data.getNumRows());
        return data;
    }
    
    private void processTestData(AquilaAnomalyDetector model, MetricFrame data) {
        final UUID uuid = model.getUuid();
        final ListIterator<Mpoint> mpoints = data.listIterator();
        while (mpoints.hasNext()) {
            final Mpoint mpoint = mpoints.next();
            final MappedMpoint mappedMpoint = new MappedMpoint(mpoint, uuid, "aquila");
            final MappedMpoint classifiedMpoint = model.classify(mappedMpoint);
            log.trace("classifiedMpoint={}", classifiedMpoint);
        }
    }
}
