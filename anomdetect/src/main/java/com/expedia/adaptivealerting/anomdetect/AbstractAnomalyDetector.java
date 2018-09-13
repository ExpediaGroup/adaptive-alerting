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
package com.expedia.adaptivealerting.anomdetect;

import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.metrics.MetricData;
import lombok.Getter;

import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Abstract base class implementing {@link AnomalyDetector}.
 *
 * @author Willie Wheeler
 */
public abstract class AbstractAnomalyDetector implements AnomalyDetector {
    
    @Getter
    private final UUID uuid;
    
    /**
     * Creates a new anomaly detector, randomly assigning a detector UUID.
     */
    public AbstractAnomalyDetector() {
        this(UUID.randomUUID());
    }
    
    /**
     * Creates a new anomaly detector with the given detector UUID.
     *
     * @param uuid Detector UUID.
     */
    public AbstractAnomalyDetector(UUID uuid) {
        notNull(uuid, "uuid can't be null");
        this.uuid = uuid;
    }
    
    /**
     * Convenience method to create an {@link AnomalyResult}.
     *
     * @param metricData Metric data.
     * @param level      Anomaly level.
     * @return Anomaly result with the detector UUID, metric data and anomaly level set.
     */
    protected AnomalyResult anomalyResult(MetricData metricData, AnomalyLevel level) {
        return new AnomalyResult(uuid, metricData, level);
    }
}
