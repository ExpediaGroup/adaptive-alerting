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
package com.expedia.adaptivealerting.anomdetect.detect.breakout.algo;

import com.expedia.adaptivealerting.anomdetect.detect.BreakoutDetectorResult;
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
     * Estimated breakout timestamp.
     */
    private Instant timestamp;

    /**
     * Estimated energy distance between the pre- and post-breakout partitions.
     */
    private Double energyDistance;

    /**
     * Median for the pre-breakout sample.
     */
    private Double preBreakoutMedian;

    /**
     * Median for the post-breakout sample.
     */
    private Double postBreakoutMedian;

    /**
     * Estimated p-value of the energy distance statistic, based on the permutation scheme described in "Leveraging
     * Cloud Data to Mitigate User Experience from 'Breaking Bad'", by James, et al.
     */
    private Double pValue;

    /**
     * Significance level used for the significance test.
     */
    private Double alpha;

    /**
     * Indicates whether the estimate is statistically significant.
     */
    private Boolean significant;
}
