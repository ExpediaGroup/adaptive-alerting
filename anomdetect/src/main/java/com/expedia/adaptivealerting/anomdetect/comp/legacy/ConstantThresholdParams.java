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
package com.expedia.adaptivealerting.anomdetect.comp.legacy;

import com.expedia.adaptivealerting.anomdetect.detector.ConstantThresholdDetector;
import com.expedia.adaptivealerting.core.anomaly.AnomalyThresholds;
import com.expedia.adaptivealerting.core.anomaly.AnomalyType;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * We currently need this in addition to {@link com.expedia.adaptivealerting.anomdetect.detector.ConstantThresholdDetector.Params}
 * because deserializing a ConstantThresholdDetector.Params requires a <code>@type</code> ID property, whereas detectors
 * stored in the legacy format don't have that.
 */
@Data
@Accessors(chain = true)
@Deprecated
public class ConstantThresholdParams {

    /**
     * Detector type: left-, right- or two-tailed.
     */
    private AnomalyType type;

    /**
     * Constant thresholds.
     */
    private AnomalyThresholds thresholds;

    public ConstantThresholdDetector.Params toNewParams() {
        return new ConstantThresholdDetector.Params()
                .setType(type)
                .setThresholds(thresholds);
    }
}
