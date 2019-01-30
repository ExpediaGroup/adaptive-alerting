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
package com.expedia.adaptivealerting.anomdetect.source;

import com.expedia.adaptivealerting.anomdetect.AnomalyDetector;
import com.expedia.adaptivealerting.anomdetect.DetectorManager;
import com.expedia.adaptivealerting.anomdetect.DetectorMapper;
import com.expedia.adaptivealerting.anomdetect.util.DetectorMeta;
import com.expedia.metrics.MetricDefinition;

import java.util.List;
import java.util.UUID;

/**
 * Detector source interfaces, supporting two major functions:
 *
 * <ul>
 *     <li>mapping a metric definition to a list of detector metas (required by
 *     {@link DetectorMapper}), and</li>
 *     <li>mapping a detector UUID to the associated detector (required by
 *     {@link DetectorManager}).</li>
 * </ul>
 *
 * @author Willie Wheeler
 */
public interface DetectorSource {
    
    /**
     * Finds the list of detector UUIDs for a given metric.
     *
     * @param metricDefinition The metric.
     * @return The mapped detector metas.
     */
    List<DetectorMeta> findDetectorMetas(MetricDefinition metricDefinition);
    
    /**
     * Finds the detector for a given detector UUID.
     *
     * @param detectorUuid The detector UUID.
     * @return The associated detector.
     */
    AnomalyDetector findDetector(UUID detectorUuid);
}
