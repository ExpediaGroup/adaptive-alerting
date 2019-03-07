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
package com.expedia.adaptivealerting.anomdetect;

import com.expedia.adaptivealerting.anomdetect.source.DetectorSource;
import com.expedia.adaptivealerting.anomdetect.util.DetectorMeta;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Component that manages a given set of anomaly detectors.
 */
@RequiredArgsConstructor
@Slf4j
public class DetectorManager {
    
    @Getter
    @NonNull
    private DetectorSource detectorSource;
    
    private final Map<UUID, AnomalyDetector> cachedDetectors = new HashMap<>();
    
    /**
     * Returns the managed detector types.
     *
     * @return Managed detector types.
     */
    public Set<String> getDetectorTypes() {
        return detectorSource.findDetectorTypes();
    }
    
    /**
     * Indicates whether this manager manages detectors of the given type.
     *
     * @param detectorType Detector type.
     * @return Boolean indicating whether this manager manages detectors of the given type.
     */
    public boolean hasDetectorType(String detectorType) {
        notNull(detectorType, "detectorType can't be null");
        return getDetectorTypes().contains(detectorType);
    }
    
    /**
     * Classifies the mapped metric data, performing detector lookup behind the scenes. Returns {@code null} if there's
     * no detector defined for the given mapped metric data.
     *
     * @param mappedMetricData Mapped metric data.
     * @return The anomaly result, or {@code null} if there's no associated detector.
     */
    public AnomalyResult classify(MappedMetricData mappedMetricData) {
        notNull(mappedMetricData, "mappedMetricData can't be null");
        
        val detector = detectorFor(mappedMetricData);
        if (detector == null) {
            log.warn("No detector for mappedMetricData={}", mappedMetricData);
            return null;
        }
        val metricData = mappedMetricData.getMetricData();
        return detector.classify(metricData);
    }
    
    private AnomalyDetector detectorFor(MappedMetricData mappedMetricData) {
        notNull(mappedMetricData, "mappedMetricData can't be null");
        
        val detectorUUID = mappedMetricData.getDetectorUUID();
        val detectorType = mappedMetricData.getDetectorType();
        val metricDef = mappedMetricData.getMetricData().getMetricDefinition();
        
        AnomalyDetector detector = cachedDetectors.get(detectorUUID);
        if (detector == null) {
            val detectorMeta = new DetectorMeta(detectorUUID, detectorType);
            detector = detectorSource.findDetector(detectorMeta, metricDef);
            cachedDetectors.put(detectorUUID, detector);
        }
        return detector;
    }
}
