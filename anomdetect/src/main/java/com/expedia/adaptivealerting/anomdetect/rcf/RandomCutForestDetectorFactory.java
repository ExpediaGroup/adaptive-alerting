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
package com.expedia.adaptivealerting.anomdetect.rcf;

import com.expedia.adaptivealerting.anomdetect.source.DetectorFactory;
import com.expedia.adaptivealerting.anomdetect.source.DetectorSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * @author Willie Wheeler
 * @author Tatjana Kamenov
 */
@Slf4j
public final class RandomCutForestDetectorFactory implements DetectorFactory<RandomCutForestAnomalyDetector> {

    private static final String RCF_DETECTOR = "rcf-detector";

    @NonNull
    private ObjectMapper objectMapper;

    private DetectorSource detectorSource;

    @Override
    public void init(Config config, DetectorSource detectorSource) {
        this.detectorSource = detectorSource;
    }

    /**
     * Create a new RCF anomaly detector.
     *
     * @param detectorUuid A new detector UUID
     *
     * @return A new anomaly detector
     */
    @Override
    public RandomCutForestAnomalyDetector create(UUID detectorUuid) {
        /*
        notNull(detectorUuid, "uuid can't be null");
        
        // TODO
        final ModelResource modelResource = modelServiceConnector.findLatestModel(detectorUuid);
        
        log.info("Loaded model: {}", modelResource);
        if (modelResource == null) {
            log.error("There is no RCF model associated with uuid: {}", detectorUuid);
            throw new RandomCutForestProcessingException("Could not find model in the modelservice for uuid:" + detectorUuid);
        }

        if (modelResource.getDetectorType().getKey().equals(RCF_DETECTOR)) {
            return new RandomCutForestAnomalyDetector(detectorUuid, modelResource);
        }

        throw new RandomCutForestProcessingException("Wrong detector type for model with uuid:" + detectorUuid);
        */
    
        // TODO Externalize this detector. [WLW]
        throw new UnsupportedOperationException("Not currently supported");
    }
}
