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
package com.expedia.adaptivealerting.kafka.util;

import com.expedia.adaptivealerting.anomdetect.comp.DefaultDetectorSource;
import com.expedia.adaptivealerting.anomdetect.comp.DetectorSource;
import com.expedia.adaptivealerting.anomdetect.comp.connector.HttpClientWrapper;
import com.expedia.adaptivealerting.anomdetect.comp.connector.ModelServiceConnector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * Detector utilities.
 */
@Slf4j
public final class DetectorUtil {
    private static final String CK_MODEL_SERVICE_URI_TEMPLATE = "model-service-base-uri";
    public static final String DELIMITER = "=";

    public static DetectorSource buildDetectorSource(Config config) {
        val uriTemplate = config.getString(CK_MODEL_SERVICE_URI_TEMPLATE);
        val connector = new ModelServiceConnector(new HttpClientWrapper(), uriTemplate, new ObjectMapper());
        return new DefaultDetectorSource(connector);
    }
}
