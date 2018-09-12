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
package com.expedia.adaptivealerting.anomdetect.control;

import com.expedia.adaptivealerting.anomdetect.AbstractAnomalyDetector;
import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.anomaly.AnomalyThresholds;
import com.expedia.metrics.MetricData;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Anomaly detector with constant thresholds for weak and strong anomalies. Supports both one- and two-tailed tests.
 *
 * @author Willie Wheeler
 */
@ToString
public final class ConstantThresholdAnomalyDetector extends AbstractAnomalyDetector {
    
    @Getter
    private final AnomalyThresholds thresholds;
    
    public ConstantThresholdAnomalyDetector(UUID uuid, AnomalyThresholds thresholds) {
        super(uuid);
        
        notNull(thresholds, "thresholds can't be null");
        this.thresholds = thresholds;
    }
    
    @Override
    public AnomalyResult classify(MetricData metricData) {
        notNull(metricData, "metricData can't be null");
        final AnomalyLevel level = thresholds.classify(metricData.getValue());
        return anomalyResult(metricData, level);
    }
}
