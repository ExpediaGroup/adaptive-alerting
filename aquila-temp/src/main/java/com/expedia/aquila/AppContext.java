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

import com.expedia.aquila.repo.DetectorModelRepo;
import com.expedia.adaptivealerting.core.data.repo.MetricDataRepo;
import com.expedia.aquila.util.ReflectionUtil;
import com.typesafe.config.Config;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

// TODO Use generics to avoid dependencies on specific component types here.
// Basically go after something similar to Spring's ApplicationContext.getBean(Class). [WLW]

/**
 * @author Willie Wheeler
 * @author Karan Shah
 */
public final class AppContext {
    private Config appConfig;
    private MetricDataRepo metricDataRepo;
    private DetectorModelRepo detectorModelRepo;
    
    public AppContext(Config appConfig) {
        notNull(appConfig, "appConfig can't be null");
        
        this.appConfig = appConfig;
    
        final Config metricDataRepoConfig = appConfig.getConfig("repositories.metric-data.repo");
        final String metricDataRepoClassName = metricDataRepoConfig.getString("class");
        this.metricDataRepo = (MetricDataRepo) ReflectionUtil.newInstance(metricDataRepoClassName);
        metricDataRepo.init(metricDataRepoConfig);
        
        final Config detectorModelRepoConfig = appConfig.getConfig("repositories.detector-model.repo");
        final String detectorModelRepoClassName = detectorModelRepoConfig.getString("class");
        this.detectorModelRepo = (DetectorModelRepo) ReflectionUtil.newInstance(detectorModelRepoClassName);
        detectorModelRepo.init(detectorModelRepoConfig);
    }
    
    public Config getAppConfig() {
        return appConfig;
    }
    
    public MetricDataRepo metricDataRepo() {
        return metricDataRepo;
    }
    
    public DetectorModelRepo aquilaAnomalyDetectorRepo() {
        return detectorModelRepo;
    }
}
