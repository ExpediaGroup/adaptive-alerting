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
package com.expedia.adaptivealerting.anomdetect.detect.outlier.algo;

import com.expedia.adaptivealerting.anomdetect.detect.OutlierDetectorResult;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorResult;
import com.expedia.metrics.MetricData;
import lombok.Getter;
import lombok.ToString;
import lombok.val;

import java.util.UUID;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

/**
 * Anomaly detector with constant threshold for weak and strong anomalies. Supports both one- and two-tailed tests.
 */
@ToString(callSuper = true)
public final class ConstantThresholdDetector extends AbstractOutlierDetector {

    @Getter
    private final ConstantThresholdParams params;

    private final AnomalyClassifier classifier;

    public ConstantThresholdDetector(UUID uuid, ConstantThresholdParams params) {
        super(uuid);
        notNull(params, "params can't be null");
        params.validate();
        this.params = params;
        this.classifier = new AnomalyClassifier(params.getType());
    }

    @Override
    public DetectorResult detect(MetricData metricData) {
        notNull(metricData, "metricData can't be null");
        val thresholds = params.getThresholds();
        val level = classifier.classify(thresholds, metricData.getValue());
        return new OutlierDetectorResult(level).setThresholds(thresholds);
    }
}
