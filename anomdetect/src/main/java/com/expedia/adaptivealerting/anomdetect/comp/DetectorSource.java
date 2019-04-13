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
package com.expedia.adaptivealerting.anomdetect.comp;

import com.expedia.adaptivealerting.anomdetect.DetectorException;
import com.expedia.adaptivealerting.anomdetect.DetectorManager;
import com.expedia.adaptivealerting.anomdetect.DetectorMapper;
import com.expedia.adaptivealerting.anomdetect.DetectorNotFoundException;
import com.expedia.adaptivealerting.anomdetect.detector.Detector;
import com.expedia.adaptivealerting.anomdetect.mapper.DetectorMatchResponse;
import com.expedia.metrics.MetricDefinition;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Detector source interfaces, supporting two major functions:
 *
 * <ul>
 * <li>mapping a metric definition to a list of detector metas (required by {@link DetectorMapper}), and</li>
 * <li>mapping a detector UUID to the associated detector (required by {@link DetectorManager}).</li>
 * </ul>
 */
public interface DetectorSource {

    Set<String> findDetectorTypes();

    /**
     * Finds the list of detector UUIDs for a given metric.
     *
     * @param metricDef The metric.
     * @return The detector UUIDs.
     * @throws DetectorException if there's a problem finding the detectors
     */
    List<UUID> findDetectorUuids(MetricDefinition metricDef);

    /**
     * Finds the detector for a given detector and, optionally, metric.
     *
     * @param uuid Detector UUID.
     * @return The associated detector.
     * @throws DetectorNotFoundException if the detector does not exist
     * @throws DetectorException         if there's some other problem while trying to finding the detector
     */
    Detector findDetector(UUID uuid);

    /**
     * Finds the list of detector UUIDs updated in last `timePeriod` minutes
     *
     * @param timePeriod time period in minutes.
     * @return The detector UUIDs.
     * @throws DetectorException if there's a problem finding the detectors
     */
    public List<UUID> findUpdatedDetectors(int timePeriod);


    public DetectorMatchResponse findMatchingDetectorMappings(List<Map<String, String>> metricTags);

}
