/*
 * Copyright 2018-2019 Expedia Group, Inc.
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
package com.expedia.aquila.core.repo;

import com.expedia.aquila.core.model.AquilaModel;
import com.expedia.aquila.core.model.AquilaModelMetadata;
import com.typesafe.config.Config;

import java.util.UUID;

// TODO At some point I expect that there will be multiple models for a given UUID. There are a number of reasons. For
// example, we will likely want to store historical models so we can stitch them together when somebody wants to see an
// aggregate model for a stretch in time. Another reason would be that even for a given point in time, we often need
// multiple models to support rolling windows for large intervals. (For example, if the time series has a four hour
// interval, then we may want to create models for every 15 minutes.) So we will need to figure out how we want to
// handle this. [WLW]

/**
 * Aquila model repository interface.
 *
 * @author Willie Wheeler
 * @author Karan Shah
 */
public interface AquilaModelRepo {
    
    /**
     * Initializes the repository.
     *
     * @param config Repository configuration.
     */
    void init(Config config);
    
    /**
     * Saves the given Aquila model, attaching the associated metadata.
     *
     * @param model    Aquila model.
     * @param metadata Aquila model metadata.
     */
    void save(AquilaModel model, AquilaModelMetadata metadata);
    
    /**
     * Loads the Aquila model for the given detector UUID.
     *
     * @param detectorUuid Detector UUID.
     * @return Aquila model.
     */
    AquilaModel load(UUID detectorUuid);
}
