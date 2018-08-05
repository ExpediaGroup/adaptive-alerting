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

import com.expedia.adaptivealerting.dataconnect.DataConnector;
import com.expedia.aquila.core.repo.DetectorModelRepo;
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
    private DataConnector dataConnector;
    private DetectorModelRepo modelConnector;
    
    public TrainerContext(Config config) {
        notNull(config, "config can't be null");
        initDataConnector(config);
        initModelConnector(config);
    }
    
    public DataConnector dataConnector() {
        return dataConnector;
    }
    
    public DetectorModelRepo aquilaAnomalyDetectorRepo() {
        return modelConnector;
    }
    
    private void initDataConnector(Config config) {
        final Config connectorConfig = config.getConfig("connectors.data");
        notNull(connectorConfig, "Property connectors.data must be defined");
        
        final String connectorClassName = connectorConfig.getString("class");
        notNull(connectorClassName, "Property connectors.data.class must be defined");
        
        this.dataConnector = (DataConnector) ReflectionUtil.newInstance(connectorClassName);
        dataConnector.init(connectorConfig);
    }
    
    private void initModelConnector(Config config) {
        final Config connectorConfig = config.getConfig("connectors.models");
        notNull(connectorConfig, "Property connectors.model must be defined");
        
        final String connectorClassName = connectorConfig.getString("class");
        notNull(connectorClassName, "Property connectors.model.class must be defined");
        
        this.modelConnector = (DetectorModelRepo) ReflectionUtil.newInstance(connectorClassName);
        modelConnector.init(connectorConfig);
    }
}
