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

import com.expedia.adaptivealerting.anomdetect.AnomalyDetector;
import com.typesafe.config.Config;

import java.util.UUID;

/**
 * Anomaly detector factory.
 *
 * @author Willie Wheeler
 */
public interface DetectorFactory<T extends AnomalyDetector> {
    
    /**
     * Initializes the factory.
     *
     * @param config         Factory configuration.
     * @param detectorSource Detector source.
     */
    void init(Config config, DetectorSource detectorSource);
    
    /**
     * Creates an anomaly detector. This would usually involve looking up at least the model parameters from persistent
     * storages, based on automated model selection and autotuning. In many cases it would involve looking up a
     * pretrained model.
     *
     * @param detectorUuid Detector UUID.
     * @return Anomaly detector, or {@literal null} if the creation attempt failed.
     */
    T create(UUID detectorUuid);
}
