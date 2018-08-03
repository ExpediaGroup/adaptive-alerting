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

import com.expedia.aquila.detect.AquilaAnomalyDetector;
import com.expedia.aquila.core.model.PredictionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * @author Willie Wheeler
 */
public abstract class AbstractDetectorModelRepo implements DetectorModelRepo {
    private static final Logger log = LoggerFactory.getLogger(AbstractDetectorModelRepo.class);
    
    @Override
    public void save(AquilaAnomalyDetector detector) {
        notNull(detector, "detector can't be null");
        final UUID uuid = getOrCreateUuid(detector);
        log.info("Saving AquilaAnomalyDetector: uuid={}", uuid);
        getPredictionModelRepo().save(uuid, detector.getPredictionModel());
    }
    
    @Override
    public AquilaAnomalyDetector load(UUID uuid) {
        notNull(uuid, "uuid can't be null");
        log.info("Loading AquilaAnomalyDetector: uuid={}", uuid);
        final PredictionModel predModel = getPredictionModelRepo().load(uuid);
        final AquilaAnomalyDetector detector = new AquilaAnomalyDetector(predModel);
        // TODO Shouldn't have to handle this ourselves. [WLW]
        detector.setUuid(uuid);
        return detector;
    }
    
    protected abstract PredictionModelRepo getPredictionModelRepo();
    
    protected UUID getOrCreateUuid(AquilaAnomalyDetector detector) {
        UUID uuid = detector.getUuid();
        if (uuid == null) {
            uuid = UUID.randomUUID();
            detector.setUuid(uuid);
        }
        return uuid;
    }
}
