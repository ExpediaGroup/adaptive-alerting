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
package com.expedia.adaptivealerting.anomdetect.breakout.edm;

import com.expedia.adaptivealerting.anomdetect.breakout.BreakoutEstimate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EdmxBreakoutEstimate implements BreakoutEstimate {

    /**
     * Estimated breakout location, or -1 if no breakout was found.
     */
    private int location;

    /**
     * Estimated energy distance between the pre- and post-breakout samples. This is a divergence measure.
     */
    private double energyDistance;

    /**
     * Estimated p-value for the energy distance.
     */
    private double pValue;

    /**
     * Significance level to apply for the significance test. The breakout is significant if the p-value is less than or
     * equal to the alpha.
     */
    private double alpha;

    /**
     * Convenience field to indicate whether the breakout is statistically significant.
     */
    private boolean significant;
}
