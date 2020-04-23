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

import com.expedia.adaptivealerting.anomdetect.detect.Detector;
import com.expedia.adaptivealerting.anomdetect.DetectorManager;
import com.expedia.adaptivealerting.anomdetect.mapper.DetectorMapper;
import com.expedia.adaptivealerting.anomdetect.mapper.DetectorMapping;
import com.expedia.adaptivealerting.anomdetect.mapper.DetectorMatchResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Detector source interface, supporting two major functions:
 *
 * <ul>
 * <li>looking up a list of detector UUIDs for a given metric (required by {@link DetectorMapper}), and</li>
 * <li>looking up a detector by UUID (required by {@link DetectorManager}).</li>
 * </ul>
 */
public interface DetectorSource {

    // TODO findDetectorMappings should take a List<MetricDefinition> as an argument, as
    //  MetricDefinition is the system's concept for metric identity. The DetectorSoruce
    //  implementation can itself pull the tags from the MetricDefinition. [WLW]

    // TODO Do the findUpdated* methods handle deleted mappings and detectors? If so (and I
    //  think they should), then we want to document that too. [WLW]

    /**
     * Finds all detector UUIDs for the given metrics.
     *
     * @param metricTags A list of metrics, each represented by a set of tags
     * @return A list of detector UUIDs mapped to the given metrics.
     */
    DetectorMatchResponse findDetectorMappings(List<Map<String, String>> metricTags);

    /**
     * Finds the list of detector mappings updated in the last {@code timePeriod} seconds. This allows the Detector
     * Mapper to keep current with new and updated mappings.
     *
     * @param timePeriod Time period in seconds. Must be strictly positive.
     * @return List of detector mappings.
     */
    List<DetectorMapping> findUpdatedDetectorMappings(long timePeriod);

    /**
     * Finds the list of detector mappings updated since {@code lastModifiedTime}.
     *
     * @param lastModifiedTime Timestamp in millisecodns. Must be strictly positive.
     * @return List of detector mappings.
     */
    List<DetectorMapping> findDetectorMappingsUpdatedSince(long lastModifiedTime);

    /**
     * Finds a detector mapping for the given detector UUID
     *
     * @param uuid Detector UUID.
     * @return Detector mapping
     */
    DetectorMapping findDetectorMappingByUuid(UUID uuid);

    /**
     * Finds the detector for a given detector and, optionally, metric.
     *
     * @param uuid Detector UUID.
     * @return The associated detector.
     * @throws DetectorException if there's a problem while trying to finding the detector
     */
    Detector findDetector(UUID uuid);

    /**
     * Finds the list of detector UUIDs updated in last {@code timePeriod} seconds. This allows the Detector Manager to
     * keep current with new and updated detectors.
     *
     * @param timePeriod Time period in seconds. Must be strictly positive.
     * @return The detector UUIDs.
     * @throws DetectorException if there's a problem finding the detectors
     */
    List<UUID> findUpdatedDetectors(long timePeriod);

    /**
     * Gets the total count of enabled detectors.
     *
     * @return The detector mapping count.
     * @throws DetectorException if there's a problem getting the detector mapping count.
     */
    long getEnabledDetectorMappingCount();
}
