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
package com.expedia.adaptivealerting.anomdetect.detect.algo;

import com.expedia.adaptivealerting.anomdetect.detect.DetectorDocument;
import com.expedia.adaptivealerting.anomdetect.detect.TypedDetectorBuilder;
import lombok.val;

import java.util.Map;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

/**
 * {@link EdmxBreakoutDetector} builder.
 */
public class EdmxBreakoutDetectorBuilder implements TypedDetectorBuilder<EdmxBreakoutDetector> {

    @Override
    public EdmxBreakoutDetector build(DetectorDocument document) {
        notNull(document, "document can't be null");
        val uuid = document.getUuid();
        val config = document.getDetectorConfig();
        val hyperparamsMap = (Map<String, Object>) config.get("hyperparams");
        val hyperparams = toHyperparams(hyperparamsMap);
        return new EdmxBreakoutDetector(uuid, hyperparams);
    }

    private EdmxBreakoutDetectorHyperparams toHyperparams(Map<String, Object> map) {
        return new EdmxBreakoutDetectorHyperparams()
                .setBufferSize((Integer) map.get("bufferSize"))
                .setDelta((Integer) map.get("delta"))
                .setNumPerms((Integer) map.get("numPerms"))
                .setAlpha((Double) map.get("alpha"));
    }
}
