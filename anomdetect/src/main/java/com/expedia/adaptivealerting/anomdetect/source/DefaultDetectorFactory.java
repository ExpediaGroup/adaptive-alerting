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
package com.expedia.adaptivealerting.anomdetect.source;

import com.expedia.adaptivealerting.anomdetect.AbstractAnomalyDetector;
import com.expedia.adaptivealerting.anomdetect.util.ModelResource;
import com.expedia.adaptivealerting.anomdetect.util.ModelServiceConnector;
import com.expedia.adaptivealerting.core.util.ReflectionUtil;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * A basic anomaly detector factory that reads anomaly detector models as JSON.
 *
 * @author Willie Wheeler
 */
@Slf4j
public final class DefaultDetectorFactory<T extends AbstractAnomalyDetector> implements DetectorFactory<T> {
    private Class<T> detectorClass;
    private ModelServiceConnector modelServiceConnector;

    @Override
    public void init(Config config, ModelServiceConnector modelServiceConnector) {
        notNull(config, "config can't be null");
        notNull(modelServiceConnector, "modelServiceConnector can't be null");

        this.detectorClass = ReflectionUtil.classForName(config.getString("detectorClass"));
        this.modelServiceConnector = modelServiceConnector;
        
        log.info("Initialized factory: detectorClass={}", detectorClass.getName());
    }

    @Override
    public T create(UUID uuid) {
        notNull(uuid, "uuid can't be null");

        final ModelResource model = modelServiceConnector.findLatestModel(uuid);
        log.info("Loaded model: {}", model);
        if (model == null) {
            log.error("No model found for uuid = {}", uuid);
            return null; // TODO: decide how to deal with this.
        }

        T detector = ReflectionUtil.newInstance(detectorClass);
        detector.init(model);
        return detector;
    }
}
