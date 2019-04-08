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
package com.expedia.adaptivealerting.anomdetect.detector;

import com.expedia.adaptivealerting.anomdetect.comp.legacy.DetectorParams;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.anomaly.AnomalyType;
import com.expedia.metrics.MetricData;

import java.util.UUID;

/**
 * Anomaly detector interface.
 */
public interface Detector<T extends DetectorParams> {

    /**
     * Returns the anomaly detector UUID.
     *
     * @return Anomaly detector UUID.
     */
    UUID getUuid();

    /**
     * Classifies a given metric data point.
     *
     * @param metricData Metric data point.
     * @return Anomaly result.
     */
    AnomalyResult classify(MetricData metricData);

    // ================================================================================
    // Deprecated
    // ================================================================================

    // Deprecated: params and anomaly type not part of the general contract.
    @Deprecated
    void init(UUID uuid, T params, AnomalyType anomalyType);

    // Deprecated: params not part of the general contract.
    // params can go with the detector or its individual components (e.g. forecasters).
    @Deprecated
    Class<T> getParamsClass();

    // Deprecated: anomalyType not part of the general contract.
    // anomalyType is for forecasting detectors.
    @Deprecated
    AnomalyType getAnomalyType();
}
