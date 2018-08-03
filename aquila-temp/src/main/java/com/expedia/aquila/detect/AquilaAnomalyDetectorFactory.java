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
package com.expedia.aquila.detect;

import com.expedia.adaptivealerting.anomdetect.AnomalyDetectorFactory;
import com.expedia.aquila.train.TrainerContext;
import com.expedia.aquila.core.repo.DetectorModelRepo;
import com.typesafe.config.Config;

import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * @author Willie Wheeler
 */
public final class AquilaAnomalyDetectorFactory implements AnomalyDetectorFactory<AquilaAnomalyDetector> {
    private DetectorModelRepo detectorModelRepo;
    
    @Override
    public void init(Config appConfig) {
        final TrainerContext trainerContext = new TrainerContext(appConfig);
        this.detectorModelRepo = trainerContext.aquilaAnomalyDetectorRepo();
    }
    
    @Override
    public AquilaAnomalyDetector create(UUID uuid) {
        notNull(uuid, "uuid can't be null");
        return detectorModelRepo.load(uuid);
    }
}
