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

import com.expedia.aquila.core.repo.DetectorModelRepo;
import com.expedia.adaptivealerting.core.data.repo.MetricDataRepo;
import com.expedia.aquila.core.util.ReflectionUtil;
import com.typesafe.config.Config;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Trainer application context.
 *
 * @author Willie Wheeler
 * @author Karan Shah
 */
public final class TrainerContext {
    private Config config;
    private MetricDataRepo datasetRepo;
    private DetectorModelRepo modelRepo;
    
    public TrainerContext(Config config) {
        notNull(config, "config can't be null");
        
        this.config = config;
    
        final Config datasetRepoConfig = config.getConfig("repositories.datasets");
        final String datasetRepoClassName = datasetRepoConfig.getString("class");
        this.datasetRepo = (MetricDataRepo) ReflectionUtil.newInstance(datasetRepoClassName);
        datasetRepo.init(datasetRepoConfig);
        
        final Config modelRepoConfig = config.getConfig("repositories.models");
        final String modelRepoClassName = modelRepoConfig.getString("class");
        this.modelRepo = (DetectorModelRepo) ReflectionUtil.newInstance(modelRepoClassName);
        modelRepo.init(modelRepoConfig);
    }
    
    public Config getConfig() {
        return config;
    }
    
    public MetricDataRepo metricDataRepo() {
        return datasetRepo;
    }
    
    public DetectorModelRepo aquilaAnomalyDetectorRepo() {
        return modelRepo;
    }
}
