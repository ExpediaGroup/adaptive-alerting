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
package com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.constant;

import com.expedia.adaptivealerting.anomdetect.detect.DetectorResult;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.AbstractOutlierDetector;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.OutlierDetectorResult;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.AnomalyClassifier;
import com.expedia.metrics.MetricData;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;

import java.util.UUID;

/**
 * Anomaly detector with constant threshold for weak and strong anomalies. Supports both one- and two-tailed tests.
 */
@ToString(callSuper = true)
public final class ConstantThresholdDetector extends AbstractOutlierDetector {
    private static final String NAME = "constant-threshold";

    @Getter
    private final ConstantThresholdDetectorParams params;
    @Getter
    private final boolean trusted;

    private final AnomalyClassifier classifier;


    public ConstantThresholdDetector(@NonNull UUID uuid, @NonNull ConstantThresholdDetectorParams params, boolean trusted) {
        super(uuid);
        params.validate();
        this.params = params;
        this.trusted = trusted;
        this.classifier = new AnomalyClassifier(params.getType());
    }

    @Override
    public DetectorResult detect(@NonNull MetricData metricData) {
        val thresholds = params.getThresholds();
        val trusted = isTrusted();
        val level = classifier.classify(thresholds, metricData.getValue());
        return new OutlierDetectorResult()
                .setAnomalyLevel(level)
                .setThresholds(thresholds)
                .setTrusted(trusted);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
