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
package com.expedia.aquila.core.repo;

import com.expedia.aquila.core.model.AquilaModel;
import com.expedia.aquila.core.model.PredictionModel;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * @author Willie Wheeler
 */
@Slf4j
public abstract class AbstractAquilaModelRepo implements AquilaModelRepo {
    
    @Override
    public void save(AquilaModel model) {
        notNull(model, "model can't be null");
        
        final UUID detectorUuid = model.getDetectorUuid();
        log.info("Saving AquilaModel: detectorUuid={}", detectorUuid);
        getPredictionModelRepo().save(detectorUuid, model.getPredictionModel());
    }
    
    @Override
    public AquilaModel load(UUID detectorUuid) {
        notNull(detectorUuid, "detectorUuid can't be null");
        
        log.info("Loading detector model: detectorUuid={}", detectorUuid);
        final PredictionModel predModel = getPredictionModelRepo().load(detectorUuid);
        return new AquilaModel(detectorUuid, predModel);
    }
    
    protected abstract PredictionModelRepo getPredictionModelRepo();
}
