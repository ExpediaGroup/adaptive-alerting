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

import com.expedia.adaptivealerting.anomdetect.detect.AnomalyLevel;
import com.expedia.adaptivealerting.anomdetect.detect.AnomalyThresholds;
import com.expedia.adaptivealerting.anomdetect.detect.AnomalyType;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

@RequiredArgsConstructor
@ToString
public class AnomalyClassifier {

    @NonNull
    private AnomalyType anomalyType;

    public AnomalyLevel classify(AnomalyThresholds thresholds, double observed) {
        notNull(thresholds, "thresholds can't be null");

        val checkUpper = (anomalyType == AnomalyType.RIGHT_TAILED || anomalyType == AnomalyType.TWO_TAILED);
        val checkLower = (anomalyType == AnomalyType.LEFT_TAILED || anomalyType == AnomalyType.TWO_TAILED);

        val upperStrong = thresholds.getUpperStrong();
        val upperWeak = thresholds.getUpperWeak();
        val lowerWeak = thresholds.getLowerWeak();
        val lowerStrong = thresholds.getLowerStrong();

        if (checkUpper) {
            if (upperStrong != null && observed >= upperStrong) {
                return AnomalyLevel.STRONG;
            } else if (upperWeak != null && observed >= upperWeak) {
                return AnomalyLevel.WEAK;
            }
        }

        if (checkLower) {
            if (lowerStrong != null && observed <= lowerStrong) {
                return AnomalyLevel.STRONG;
            } else if (lowerWeak != null && observed <= lowerWeak) {
                return AnomalyLevel.WEAK;
            }
        }

        return AnomalyLevel.NORMAL;
    }
}
