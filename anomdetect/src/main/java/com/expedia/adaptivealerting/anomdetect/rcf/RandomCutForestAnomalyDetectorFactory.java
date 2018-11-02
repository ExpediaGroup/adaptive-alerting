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
package com.expedia.adaptivealerting.anomdetect.rcf;

import com.expedia.adaptivealerting.anomdetect.AnomalyDetectorFactory;
import com.expedia.adaptivealerting.anomdetect.util.ModelResource;
import com.expedia.adaptivealerting.anomdetect.util.ModelServiceConnector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Resources;

/**
 * @author Willie Wheeler
 * @author Tatjana Kamenov
 */
@Slf4j
public final class RandomCutForestAnomalyDetectorFactory implements AnomalyDetectorFactory<RandomCutForestAnomalyDetector> {

    private static final String RCF_DETECTOR = "rcf-detector";
    @NonNull
    private ObjectMapper objectMapper;

    private ModelServiceConnector connector;

    @Override
    public void init(Config config, ModelServiceConnector connector) {
        this.connector = connector;
    }

    @Override
    public RandomCutForestAnomalyDetector create(UUID uuid) {
        notNull(uuid, "uuid can't be null");
        final Resources<ModelResource> models = connector.findModels(uuid);
        final Collection<ModelResource> modelResources = models.getContent();

        List<ModelResource> modelResourceList = new ArrayList<>(modelResources);
        if (modelResourceList.isEmpty()) {
            log.error("There is no model associated with uuid: {}", uuid);
            throw new RandomCutForestProcessingException("There is no model associated with uuid:" + uuid);
        }

        // TODO [TK] we are not supporting multiple models for a single uuid for now, just grab first detector
        final ModelResource modelResource = modelResourceList.get(0);
        log.info("Found NEW model: {}", modelResource);

        if (modelResource.getDetectorType().getKey().equals(RCF_DETECTOR)) {
            return new RandomCutForestAnomalyDetector(uuid, modelResource);
        }

        throw new RandomCutForestProcessingException("Wrong detector type for model with uuid:" + uuid);
    }
}
