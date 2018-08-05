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
package com.expedia.aquila.core.model;

import com.expedia.adaptivealerting.core.util.DateUtil;

import java.time.Instant;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * @author Willie Wheeler
 * @author Karan Shah
 */
public final class MidpointModel {
    private static final int MINUTES_PER_WEEK = 7 * 24 * 60;
    
    private DecompType type;
    private double[] seasonal;
    private double trend;
    
    /**
     * To support deserialization.
     */
    public MidpointModel() {
    }
    
    public MidpointModel(DecompType type, double[] seasonal, double trend) {
        notNull(type, "type can't be null");
        notNull(seasonal, "seasonal can't be null");
        this.type = type;
        this.seasonal = seasonal;
        this.trend = trend;
    }
    
    public DecompType getType() {
        return type;
    }
    
    public void setType(DecompType type) {
        this.type = type;
    }
    
    public double[] getSeasonal() {
        return seasonal;
    }
    
    public void setSeasonal(double[] seasonal) {
        this.seasonal = seasonal;
    }
    
    public double getTrend() {
        return trend;
    }
    
    public void setTrend(double trend) {
        this.trend = trend;
    }
    
    public double predict(Instant date) {
        notNull(date, "date can't be null");
        
        final int tickSize = MINUTES_PER_WEEK / seasonal.length;
        final int index = DateUtil.tickOffsetFromWeekStart(date, tickSize);
        
        switch (type) {
            case ADDITIVE:
                return seasonal[index] + trend;
            case MULTIPLICATIVE:
                return seasonal[index] * trend;
            default:
                throw new IllegalStateException("Illegal type: " + type);
        }
    }
}
