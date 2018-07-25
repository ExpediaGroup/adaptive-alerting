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
package com.expedia.aquila.model;

import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;

/**
 * @author Willie Wheeler
 * @author Karan Shah
 */
public final class Classification {
    private double weakThresholdSigmas;
    private double strongThresholdSigmas;
    private double anomalyScore;
    private AnomalyLevel anomalyLevel;
    
    public Classification(
            double weakThresholdSigmas,
            double strongThresholdSigmas,
            double anomalyScore,
            AnomalyLevel anomalyLevel) {
        
        this.weakThresholdSigmas = weakThresholdSigmas;
        this.strongThresholdSigmas = strongThresholdSigmas;
        this.anomalyScore = anomalyScore;
        this.anomalyLevel = anomalyLevel;
    }
    
    public double getWeakThresholdSigmas() {
        return weakThresholdSigmas;
    }
    
    public double getStrongThresholdSigmas() {
        return strongThresholdSigmas;
    }
    
    public double getAnomalyScore() {
        return anomalyScore;
    }
    
    public AnomalyLevel getAnomalyLevel() {
        return anomalyLevel;
    }
}
