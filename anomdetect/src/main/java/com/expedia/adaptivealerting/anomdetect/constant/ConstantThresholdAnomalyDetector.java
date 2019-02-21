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
package com.expedia.adaptivealerting.anomdetect.constant;

import com.expedia.adaptivealerting.anomdetect.AbstractAnomalyDetector;
import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.anomaly.AnomalyThresholds;
import com.expedia.adaptivealerting.core.anomaly.AnomalyType;
import com.expedia.metrics.MetricData;
import lombok.Data;
import lombok.NonNull;

import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Anomaly detector with constant thresholds for weak and strong anomalies. Supports both one- and two-tailed tests.
 */
@Data
public final class ConstantThresholdAnomalyDetector extends AbstractAnomalyDetector<ConstantThresholdParams> {

    @NonNull
    private ConstantThresholdParams params;

    public ConstantThresholdAnomalyDetector() {
        this(UUID.randomUUID(), new ConstantThresholdParams());
    }

    public ConstantThresholdAnomalyDetector(ConstantThresholdParams params) {
        this(UUID.randomUUID(), params);
    }

    public ConstantThresholdAnomalyDetector(UUID uuid, ConstantThresholdParams params) {
        notNull(uuid, "uuid can't be null");
        notNull(params, "params can't be null");

        setUuid(uuid);
        loadParams(params);
    }

    @Override
    protected Class<ConstantThresholdParams> getParamsClass() {
        return ConstantThresholdParams.class;
    }

    @Override
    protected void loadParams(ConstantThresholdParams params) {
        this.params = params;
    }

    @Override
    public AnomalyResult classify(MetricData metricData) {
        notNull(metricData, "metricData can't be null");
        final AnomalyThresholds thresholds = params.getThresholds();
        final AnomalyType type = params.getType();
        final AnomalyLevel level = thresholds.classify(type, metricData.getValue());
        return new AnomalyResult(getUuid(), metricData, level);
    }
}
