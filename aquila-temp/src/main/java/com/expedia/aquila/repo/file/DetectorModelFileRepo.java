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
package com.expedia.aquila.repo.file;

import com.expedia.aquila.AquilaAnomalyDetector;
import com.expedia.aquila.repo.DetectorModelRepo;
import com.typesafe.config.Config;

import java.io.File;
import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * @author Willie Wheeler
 */
public class DetectorModelFileRepo implements DetectorModelRepo {
    private PredictionModelFileRepo predModelRepo;
    
    @Override
    public void init(Config config) {
        notNull(config, "config can't be null");
        final File baseDir = new File(config.getString("base.dir"));
        this.predModelRepo = new PredictionModelFileRepo(baseDir);
    }
    
    @Override
    public void save(AquilaAnomalyDetector detector) {
        notNull(detector, "detector can't be null");
        final UUID uuid = getOrCreateUuid(detector);
        predModelRepo.save(uuid, detector.getPredictionModel());
    }
    
    @Override
    public AquilaAnomalyDetector load(UUID uuid) {
        notNull(uuid, "uuid can't be null");
        return new AquilaAnomalyDetector(predModelRepo.load(uuid));
    }
    
    private UUID getOrCreateUuid(AquilaAnomalyDetector detector) {
        UUID uuid = detector.getUuid();
        if (uuid == null) {
            uuid = UUID.randomUUID();
            detector.setUuid(uuid);
        }
        return uuid;
    }
}
