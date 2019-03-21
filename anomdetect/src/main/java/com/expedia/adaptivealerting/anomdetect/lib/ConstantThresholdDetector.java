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
package com.expedia.adaptivealerting.anomdetect.lib;

import com.expedia.adaptivealerting.anomdetect.core.AbstractDetector;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.metrics.MetricData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Anomaly detector with constant thresholds for weak and strong anomalies. Supports both one- and two-tailed tests.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public final class ConstantThresholdDetector extends AbstractDetector<ConstantThresholdParams> {

    public ConstantThresholdDetector() {
        super(ConstantThresholdParams.class);
    }

    @Override
    public AnomalyResult classify(MetricData metricData) {
        notNull(metricData, "metricData can't be null");

        val params = getParams();
        val thresholds = params.getThresholds();
        val level = thresholds.classify(metricData.getValue());

        val result = new AnomalyResult(level);
        result.setThresholds(thresholds);
        return result;
    }
}
