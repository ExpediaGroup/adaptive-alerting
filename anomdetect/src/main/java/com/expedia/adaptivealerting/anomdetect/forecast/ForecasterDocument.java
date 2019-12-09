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
package com.expedia.adaptivealerting.anomdetect.forecast;

import com.expedia.adaptivealerting.anomdetect.util.DocumentMeta;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorDocument;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.UUID;

/**
 * <p>
 * Component document. "Components" are detectors and their important parts, like forecasters. This class allows us to
 * store model training outputs (whether detectors or forecasters or anything else) in a persistent store. The common
 * representation also allows us to take a polyglot approach to model training and inference. For example we might train
 * a model using Python, store it as JSON in the persistent store, and then read it into a Java inference engine.
 * </p>
 * <p>
 * Note: Currently we're using {@link DetectorDocument} for detectors.
 * We will switch over to this class in the future.
 * </p>
 */
@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForecasterDocument {

    /**
     * Component UUID.
     */
    private UUID uuid;

    /**
     * A key representing the component type, such as "ewma-forecaster" or "cusum-detector". This allows factories to
     * know which sort of component to build.
     */
    private String type;

    /**
     * Component metadata.
     */
    private DocumentMeta meta;

    /**
     * Model configuration. Usually this would be model parameters but it can be arbitrary configuration.
     */
    private Map<String, Object> config;
}
