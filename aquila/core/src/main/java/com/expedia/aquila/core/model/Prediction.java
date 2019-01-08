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
package com.expedia.aquila.core.model;

// FIXME I don't think this is quite an interval prediction (see Wikipedia). Moreover, to use it to generate useful
// interval predictions (e.g., confidence intervals), we need to have n as well. But our current approach to estimating
// dispersion doesn't involve a specific n. So we will need to rename/rework some of this. [WLW]

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An interval prediction.
 *
 * @author Willie Wheeler
 * @author Karan Shah
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public final class Prediction {
    private double mean;
    private double stdev;
}
