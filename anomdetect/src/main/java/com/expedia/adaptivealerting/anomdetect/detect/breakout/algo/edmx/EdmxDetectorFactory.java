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
package com.expedia.adaptivealerting.anomdetect.detect.breakout.algo.edmx;

import com.expedia.adaptivealerting.anomdetect.detect.breakout.algo.edmx.EdmxDetector;
import com.expedia.adaptivealerting.anomdetect.detect.breakout.algo.edmx.EdmxHyperparams;
import com.expedia.adaptivealerting.anomdetect.source.DetectorDocument;
import com.expedia.adaptivealerting.anomdetect.source.DetectorFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class EdmxDetectorFactory implements DetectorFactory<EdmxDetector> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @NonNull
    private DetectorDocument document;

    @Override
    public EdmxDetector buildDetector() {
        // The EDM-X detector fits a new model with each metric point.
        // That's why we're using hyperparameters instead of parameters here.
        val hyperparamsMap = document.getDetectorConfig().get("hyperparams");
        val hyperparams = objectMapper.convertValue(hyperparamsMap, EdmxHyperparams.class);
        return new EdmxDetector(document.getUuid(), hyperparams);
    }
}
