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
package com.expedia.adaptivealerting.anomdetect.aquila;

import com.expedia.adaptivealerting.anomdetect.source.DetectorFactory;
import com.expedia.adaptivealerting.anomdetect.source.DetectorSource;
import com.expedia.metrics.jackson.MetricsJavaModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

// TODO Move this class to the Aquila repo. [WLW]

/**
 * @author Willie Wheeler
 */
@Slf4j
public class AquilaDetectorFactory implements DetectorFactory<AquilaAnomalyDetector> {
    private static final String MODEL_UUID_PARAM = "modelUuid";

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new MetricsJavaModule());
    private String uri;
    private DetectorSource detectorSource;
    
    @Override
    public void init(Config config, DetectorSource detectorSource) {
        this.uri = config.getString("uri");
        this.detectorSource = detectorSource;
        log.info("Initialized AquilaFactory: uri={}", uri);
    }
    
    @Override
    public AquilaAnomalyDetector create(UUID detectorUuid) {
        /*
        notNull(detectorUuid, "uuid can't be null");
        
        // TODO
        final ModelResource model = modelServiceConnector.findLatestModel(detectorUuid);
        
        log.info("Loaded model: {}", model);
        if (model == null
                || model.getParams() == null
                || model.getParams().get(MODEL_UUID_PARAM) == null) {
            throw new RuntimeException("Valid model not found for uuid=" + detectorUuid);
        }

        return new AquilaAnomalyDetector(
                objectMapper,
                uri,
                UUID.fromString(model.getParams().get(MODEL_UUID_PARAM).toString())
        );
        */
        
        // TODO Externalize this detector. [WLW]
        throw new UnsupportedOperationException("Not currently supported");
    }
}
