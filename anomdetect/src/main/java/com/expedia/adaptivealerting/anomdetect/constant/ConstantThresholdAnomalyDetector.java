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
package com.expedia.adaptivealerting.anomdetect.constant;

import com.expedia.adaptivealerting.anomdetect.AnomalyDetectorModel;
import com.expedia.adaptivealerting.anomdetect.BasicAnomalyDetector;
import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.anomaly.AnomalyThresholds;
import com.expedia.metrics.MetricData;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Anomaly detector with constant thresholds for weak and strong anomalies. Supports both one- and two-tailed tests.
 *
 * @author Willie Wheeler
 */
@Data
@RequiredArgsConstructor
public final class ConstantThresholdAnomalyDetector implements BasicAnomalyDetector {
    
    @NonNull
    private final UUID uuid;
    
    @NonNull
    private ConstantThresholdParams params;

    public ConstantThresholdAnomalyDetector() {
        this(UUID.randomUUID(), new ConstantThresholdParams());
    }

    public ConstantThresholdAnomalyDetector(ConstantThresholdParams params) {
        this(UUID.randomUUID(), params);
    }

    private void loadParams(ConstantThresholdParams params) {
        this.params = params;
    }
    @Override
    public void init(AnomalyDetectorModel anomalyDetectorModel) {
        if (anomalyDetectorModel instanceof ConstantThresholdModel) {
            loadParams(((ConstantThresholdModel) anomalyDetectorModel).getParams());
        }
    }
    
    @Override
    public AnomalyResult classify(MetricData metricData) {
        notNull(metricData, "metricData can't be null");
        final AnomalyThresholds thresholds = params.getThresholds();
        final AnomalyLevel level = thresholds.classify(metricData.getValue());
        return new AnomalyResult(uuid, metricData, level);
    }
}
