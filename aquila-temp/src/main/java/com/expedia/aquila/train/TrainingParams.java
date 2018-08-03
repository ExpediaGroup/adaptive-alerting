/*
 * Copyright 2018 Expedia Group, Inc.
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
package com.expedia.aquila.train;

import com.expedia.aquila.core.model.DecompType;

/**
 * Parameters for the Aquila training algorithm. Provides a fluent interface to make it easy to understand which
 * parameters are being set.
 *
 * @author Willie Wheeler
 * @author Karan Shah
 */
public final class TrainingParams {
    
    // Data
    private int tickSize;
    
    // Midpoint
    private DecompType decompType;
    private int periodSize;
    private int wmaWindowSize;
    
    /**
     * Creates a params instance with the following default values:
     *
     * <ul>
     * <li>tickSize: 5</li>
     * <li>decompType: {@link DecompType#MULTIPLICATIVE}</li>
     * <li>periodSize: 2016</li>
     * <li>wmaWindowSize: 21</li>
     * </ul>
     */
    public TrainingParams() {
        this.tickSize = 5;
        this.decompType = DecompType.MULTIPLICATIVE;
        this.periodSize = 7 * 24 * 60 / tickSize;
        this.wmaWindowSize = 21;
    }
    
    /**
     * Returns the tick size, in minutes.
     *
     * @return Tick size, in minutes.
     */
    public int tickSize() {
        return tickSize;
    }
    
    public TrainingParams tickSize(int tickSize) {
        this.tickSize = tickSize;
        return this;
    }
    
    /**
     * Returns the midpoint model decomposition type.
     *
     * @return Midpoint model decomposition type.
     */
    public DecompType decompType() {
        return decompType;
    }
    
    public TrainingParams decompType(DecompType decompType) {
        this.decompType = decompType;
        return this;
    }
    
    /**
     * Returns the seasonality period size, in ticks.
     *
     * @return Seasonality period size, in ticks.
     */
    public int periodSize() {
        return periodSize;
    }
    
    public TrainingParams periodSize(int periodSize) {
        this.periodSize = periodSize;
        return this;
    }
    
    /**
     * Returns the window size for the midpoint weighted moving average smoother, in ticks.
     *
     * @return WMA window size, in ticks.
     */
    public int wmaWindowSize() {
        return wmaWindowSize;
    }
    
    /**
     * Sets the WMA window size, adjusting upward to the next odd size if the size isn't already odd. For example,
     * providing 20 as an argument results in the actual value being set to 21.
     *
     * @param wmaWindowSize Weighted moving average window size. Rounded up to the next odd number if it's not already
     *                      odd.
     * @return {@code this} params instance.
     */
    public TrainingParams wmaWindowSize(int wmaWindowSize) {
        this.wmaWindowSize = 2 * (wmaWindowSize / 2) + 1;
        return this;
    }
}
