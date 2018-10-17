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
package com.expedia.adaptivealerting.anomdetect;

import com.expedia.adaptivealerting.anomdetect.util.ModelResource;
import com.expedia.adaptivealerting.anomdetect.util.ModelServiceConnector;
import com.expedia.adaptivealerting.core.util.ReflectionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * <p>
 * A basic anomaly detector factory that reads anomaly detector models as JSON from Amazon S3.
 * </p>
 * <p>
 * At some point we'll decouple this from Amazon, but for now this is fine.
 * </p>
 *
 * @author Willie Wheeler
 */
@Slf4j
public final class BasicAnomalyDetectorFactory<T extends BasicAnomalyDetector> implements AnomalyDetectorFactory<T> {
    private Class<T> detectorClass;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void init(Config config) {
        notNull(config, "config can't be null");

        this.detectorClass = ReflectionUtil.classForName(config.getString("detectorClass"));
        log.info("Initialized factory: detectorClass={}", detectorClass.getName());
    }

    @Override
    public T create(UUID uuid, ModelServiceConnector modelServiceConnector) {
        notNull(uuid, "uuid can't be null");

        final Resources<ModelResource> modelResources = modelServiceConnector.findModels(uuid);
        final Collection<ModelResource> modelResourceCollection = modelResources.getContent();

        List<ModelResource> modelResourceList = new ArrayList<>(modelResourceCollection);

        if (modelResourceList.size() < 1) {
            log.error("No model found for uuid = {}", uuid);
        }
        final AnomalyDetectorModel model = modelResourceList.get(0);
        log.info("Loaded model: {}", modelResourceList.get(0));

        T detector = ReflectionUtil.newInstance(detectorClass);
        detector.init(model);
        return detector;
    }
}
