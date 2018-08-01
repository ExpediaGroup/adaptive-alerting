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

import com.expedia.adaptivealerting.core.data.MetricFrame;
import com.expedia.adaptivealerting.core.data.repo.MetricDataFileRepo;
import com.expedia.adaptivealerting.core.data.repo.MetricDataRepo;
import com.expedia.aquila.repo.DetectorModelRepo;
import com.expedia.aquila.repo.file.DetectorModelFileRepo;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

/**
 * Integration test for Aquila model builds. This includes the following
 *
 * <ul>
 * <li>Load training data from the file system</li>
 * <li>Train an Aquila model</li>
 * <li>Store the model to the file system</li>
 * <li>Load the model into an anomaly detector</li>
 * <li>Load test data from the file system</li>
 * <li>Run the test data through the model</li>
 * </ul>
 *
 * @author Willie Wheeler
 */
public class AquilaModelBuildIntegrationTest {
    private static MetricDataRepo metricDataRepo;
    private static DetectorModelRepo detectorModelRepo;
    
    @BeforeClass
    public static void setUpClass() {
        final Config appConfig = ConfigFactory.load("application-file.conf");
        final Config aquilaConfig = appConfig.getConfig("aquila-detector");
        final Config reposConfig = aquilaConfig.getConfig("repositories");
        final Config dataRepoConfig = reposConfig.getConfig("metric-data.repo");
        final Config modelRepoConfig = reposConfig.getConfig("detector-model.repo");
        
        metricDataRepo = new MetricDataFileRepo();
        metricDataRepo.init(dataRepoConfig);
        
        detectorModelRepo = new DetectorModelFileRepo();
        detectorModelRepo.init(modelRepoConfig);
    }
    
    @Before
    public void setUp() {
    }
    
    @Test
    public void testModelBuild() {
        final MetricFrame trainingData = loadTrainingData();
        final AquilaAnomalyDetector trainedModel = trainModel(trainingData);
        final UUID uuid = storeModel(trainedModel);
        
        final AquilaAnomalyDetector loadedModel = loadModel(uuid);
        final MetricFrame testData = loadTestData();
        processTestData(loadedModel, testData);
    }
    
    private MetricFrame loadTrainingData() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    private AquilaAnomalyDetector trainModel(MetricFrame data) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    private UUID storeModel(AquilaAnomalyDetector model) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    private AquilaAnomalyDetector loadModel(UUID uuid) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    private MetricFrame loadTestData() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    private void processTestData(AquilaAnomalyDetector model, MetricFrame data) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
