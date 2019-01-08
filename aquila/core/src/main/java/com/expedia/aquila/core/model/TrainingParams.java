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

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Parameters for the Aquila training algorithm. Provides a fluent interface to make it easy to understand which
 * parameters are being set.
 *
 * @author Willie Wheeler
 * @author Karan Shah
 */
@Data
@AllArgsConstructor
public final class TrainingParams {
    private int intervalInMinutes;
    private DecompType decompType;
    private int periodSize;
    private int wmaWindowSize;
    
    /**
     * Creates a params instance with the following default values:
     *
     * <ul>
     * <li>intervalInMinutes: 5</li>
     * <li>decompType: {@link DecompType#MULTIPLICATIVE}</li>
     * <li>periodSize: 2016</li>
     * <li>wmaWindowSize: 21</li>
     * </ul>
     */
    public TrainingParams() {
        this.intervalInMinutes = 5;
        this.decompType = DecompType.MULTIPLICATIVE;
        this.periodSize = 7 * 24 * 60 / intervalInMinutes;
        this.wmaWindowSize = 21;
    }
    
    /**
     * Sets the WMA window size, adjusting upward to the next odd size if the size isn't already odd. For example,
     * providing 20 as an argument results in the actual value being set to 21.
     *
     * @param wmaWindowSize Weighted moving average window size. Rounded up to the next odd number if it's not already
     *                      odd.
     */
    public void setWmaWindowSize(int wmaWindowSize) {
        this.wmaWindowSize = 2 * (wmaWindowSize / 2) + 1;
    }
}
