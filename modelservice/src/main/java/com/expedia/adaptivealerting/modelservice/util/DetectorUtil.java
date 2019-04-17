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
package com.expedia.adaptivealerting.modelservice.util;

import com.expedia.adaptivealerting.anomdetect.comp.connector.ModelResource;
import com.expedia.adaptivealerting.anomdetect.comp.connector.ModelTypeResource;
import com.expedia.adaptivealerting.anomdetect.comp.legacy.LegacyDetectorFactory;
import com.expedia.adaptivealerting.anomdetect.detector.Detector;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class DetectorUtil {

    public static Detector getDetector(String detectorType, Map paramsMap) {
        ModelTypeResource modelTypeResource = new ModelTypeResource();
        modelTypeResource.setKey(detectorType);
        ModelResource resource = new ModelResource();
        resource.setDetectorType(modelTypeResource);
        resource.setParams(paramsMap);
        resource.setDateCreated(new Date());

        LegacyDetectorFactory factory = new LegacyDetectorFactory();
        return factory.createDetector(UUID.randomUUID(), resource);
    }
}
