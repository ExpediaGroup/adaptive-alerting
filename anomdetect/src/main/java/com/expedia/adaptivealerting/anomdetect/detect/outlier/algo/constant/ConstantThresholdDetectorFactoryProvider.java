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
package com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.constant;

import com.expedia.adaptivealerting.anomdetect.source.DetectorDocument;
import com.expedia.adaptivealerting.anomdetect.source.DetectorFactoryProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

public class ConstantThresholdDetectorFactoryProvider implements DetectorFactoryProvider<ConstantThresholdDetector> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ConstantThresholdDetector buildDetector(DetectorDocument document) {
        notNull(document, "document can't be null");
        val params = parseParams(document);
        return new ConstantThresholdDetector(document.getUuid(), params, document.isTrusted());
    }

    private ConstantThresholdDetectorParams parseParams(DetectorDocument document) {
        val paramsMap = document.getConfig().get("params");
        return objectMapper.convertValue(paramsMap, ConstantThresholdDetectorParams.class);
    }

}
