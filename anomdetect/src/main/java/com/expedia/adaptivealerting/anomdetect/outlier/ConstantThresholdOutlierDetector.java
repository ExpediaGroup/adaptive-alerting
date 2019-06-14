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
package com.expedia.adaptivealerting.anomdetect.outlier;

import com.expedia.adaptivealerting.anomdetect.DetectorConfig;
import com.expedia.adaptivealerting.anomdetect.DetectorResult;
import com.expedia.metrics.MetricData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;

import java.util.UUID;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

/**
 * Anomaly detector with constant threshold for weak and strong anomalies. Supports both one- and two-tailed tests.
 */
public final class ConstantThresholdOutlierDetector extends AbstractOutlierDetector {

    @Getter
    private final Params params;

    private final AnomalyClassifier classifier;

    public ConstantThresholdOutlierDetector(UUID uuid, Params params) {
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
        return new AnomalyResult(level).setThresholds(thresholds);
    }

    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = false)
    public static final class Params implements DetectorConfig {

        /**
         * Detector type: left-, right- or two-tailed.
         */
        private AnomalyType type;

        /**
         * Constant thresholds.
         */
        private AnomalyThresholds thresholds;

        @Override
        public void validate() {
            notNull(type, "type can't be null");
            notNull(thresholds, "thresholds can't be null");
        }
    }
}
