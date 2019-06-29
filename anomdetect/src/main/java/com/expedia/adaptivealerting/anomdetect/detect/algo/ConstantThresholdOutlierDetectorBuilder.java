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

import com.expedia.adaptivealerting.anomdetect.detect.AnomalyThresholds;
import com.expedia.adaptivealerting.anomdetect.detect.AnomalyType;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorDocument;
import com.expedia.adaptivealerting.anomdetect.detect.TypedDetectorBuilder;
import lombok.val;

import java.util.Map;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

/**
 * {@link ConstantThresholdOutlierDetector} builder.
 */
public class ConstantThresholdOutlierDetectorBuilder implements TypedDetectorBuilder<ConstantThresholdOutlierDetector> {

    @Override
    public ConstantThresholdOutlierDetector build(DetectorDocument document) {
        notNull(document, "document can't be null");
        val uuid = document.getUuid();
        val config = document.getDetectorConfig();
        val paramsMap = (Map<String, Object>) config.get("params");
        val params = toParams(paramsMap);
        return new ConstantThresholdOutlierDetector(uuid, params);
    }

    private ConstantThresholdOutlierDetectorParams toParams(Map<String, Object> map) {
        return new ConstantThresholdOutlierDetectorParams()
                .setType(AnomalyType.valueOf((String) map.get("type")))
                .setThresholds(toThresholds((Map<String, Object>) map.get("thresholds")));
    }

    private AnomalyThresholds toThresholds(Map<String, Object> map) {
        return new AnomalyThresholds(
                (Double) map.get("upperStrong"),
                (Double) map.get("upperWeak"),
                (Double) map.get("lowerWeak"),
                (Double) map.get("lowerStrong"));
    }
}
