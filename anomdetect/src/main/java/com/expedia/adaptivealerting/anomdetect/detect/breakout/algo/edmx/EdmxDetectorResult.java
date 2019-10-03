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

import com.expedia.adaptivealerting.anomdetect.detect.AnomalyLevel;
import com.expedia.adaptivealerting.anomdetect.detect.breakout.BreakoutDetectorResult;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.Instant;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class EdmxDetectorResult implements BreakoutDetectorResult {

    /**
     * Indicates whether the detector is warming up.
     */
    private boolean warmup;

    /**
     * Estimated breakout timestamp. This is the timestamp for the breakout itself, not the timestamp for when we found
     * the breakout.
     */
    private Instant timestamp;

    /**
     * Breakout estimate.
     */
    private EdmxEstimate edmxEstimate;

    /**
     * Indicates the strength of the anomaly.
     */
    private AnomalyLevel anomalyLevel;
}
