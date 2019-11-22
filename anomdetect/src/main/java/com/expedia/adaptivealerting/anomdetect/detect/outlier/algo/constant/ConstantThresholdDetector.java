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

import com.expedia.adaptivealerting.anomdetect.detect.outlier.OutlierDetectorResult;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorResult;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.AbstractOutlierDetector;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.AnomalyClassifier;
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
    String NAME = "constant";

    @Getter
    private final ConstantThresholdDetectorParams params;

    private final AnomalyClassifier classifier;

    @Getter
    private final boolean trusted;

    public ConstantThresholdDetector(UUID uuid, ConstantThresholdDetectorParams params, boolean trusted) {
        super(uuid);
        notNull(params, "params can't be null");
        params.validate();
        this.params = params;
        this.trusted = trusted;
        this.classifier = new AnomalyClassifier(params.getType());
    }

    @Override
    public DetectorResult detect(MetricData metricData) {
        notNull(metricData, "metricData can't be null");
        val thresholds = params.getThresholds();
        val trusted = isTrusted();
        val level = classifier.classify(thresholds, metricData.getValue());
        OutlierDetectorResult outlierResult = new OutlierDetectorResult()
                .setAnomalyLevel(level)
                .setThresholds(thresholds)
                .setTrusted(trusted);
        return outlierResult;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
