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
package com.expedia.adaptivealerting.anomdetect.detect;

import com.expedia.metrics.MetricData;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

/**
 * <p>
 * Wraps an endpoint with a representation that includes anomaly detection information.
 * </p>
 * <p>
 * By contract the {@link MetricData} must be set.
 * </p>
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public final class MappedMetricData {

    @NonNull
    private MetricData metricData;

    @NonNull
    private UUID detectorUuid;

    // Calling this anomalyResult because this is the original name for this property,
    // and various downstream processes assume this name.
    private DetectorResult anomalyResult;

    public MappedMetricData(MappedMetricData orig, DetectorResult anomalyResult) {
        notNull(orig, "orig can't be null");
        notNull(anomalyResult, "anomalyResult can't be null");

        this.metricData = orig.getMetricData();
        this.detectorUuid = orig.getDetectorUuid();
        this.anomalyResult = anomalyResult;
    }
}
