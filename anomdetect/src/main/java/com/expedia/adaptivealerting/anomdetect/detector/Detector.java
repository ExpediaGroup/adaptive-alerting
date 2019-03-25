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

import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.metrics.MetricData;

import java.util.UUID;

/**
 * Anomaly detector interface.
 */
public interface Detector<T extends DetectorParams> {

    void init(UUID uuid, T params);

    /**
     * Returns the anomaly detector UUID.
     *
     * @return Anomaly detector UUID.
     */
    UUID getUuid();

    Class<T> getParamsClass();

    /**
     * Classifies a given metric data point.
     *
     * @param metricData Metric data point.
     * @return Anomaly result.
     */
    AnomalyResult classify(MetricData metricData);
}
