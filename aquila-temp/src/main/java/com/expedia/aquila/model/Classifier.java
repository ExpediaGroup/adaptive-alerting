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
import com.expedia.adaptivealerting.core.data.Mpoint;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

// TODO Support single-tailed tests. For bookings we want to exclude "positive anomalies". [WLW]

/**
 * @author Willie Wheeler
 * @author Karan Shah
 */
public final class Classifier {
    public enum AnomalyType {
        POSITIVE,
        NEGATIVE,
        BOTH
    };
    
    private final AnomalyType anomalyType;
    private final double weakThresholdSigmas;
    private final double strongThresholdSigmas;
    
    public Classifier() {
        this(AnomalyType.BOTH, 3.0, 4.0);
    }
    
    public Classifier(AnomalyType anomalyType, double weakThresholdSigmas, double strongThresholdSigmas) {
        this.anomalyType = anomalyType;
        this.weakThresholdSigmas = weakThresholdSigmas;
        this.strongThresholdSigmas = strongThresholdSigmas;
    }
    
    public AnomalyType getAnomalyType() {
        return anomalyType;
    }
    
    public double getWeakThresholdSigmas() {
        return weakThresholdSigmas;
    }
    
    public double getStrongThresholdSigmas() {
        return strongThresholdSigmas;
    }
    
    public Classification classify(Mpoint mpoint, Prediction pred) {
        notNull(mpoint, "mpoint can't be null");
        notNull(mpoint, "pred can't be null");
        
        final double observed = mpoint.getValue().doubleValue();
        final double error = observed - pred.getMean();
        final double sigmas = error / pred.getStdev();
        
        AnomalyLevel level = AnomalyLevel.NORMAL;
        if (sigmas >= 0) {
            if (anomalyType == AnomalyType.POSITIVE || anomalyType == AnomalyType.BOTH) {
                if (sigmas >= strongThresholdSigmas) {
                    level = AnomalyLevel.STRONG;
                } else if (sigmas >= weakThresholdSigmas) {
                    level = AnomalyLevel.WEAK;
                }
            }
        } else {
            if (anomalyType == AnomalyType.NEGATIVE || anomalyType == AnomalyType.BOTH) {
                if (-sigmas >= strongThresholdSigmas) {
                    level = AnomalyLevel.STRONG;
                } else if (-sigmas >= weakThresholdSigmas) {
                    level = AnomalyLevel.WEAK;
                }
            }
        }
        
        return new Classification(weakThresholdSigmas, strongThresholdSigmas, error, level);
    }
}
