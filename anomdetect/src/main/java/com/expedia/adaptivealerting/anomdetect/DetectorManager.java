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
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.typesafe.config.Config;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Component that manages a given set of anomaly detectors.
 */
@RequiredArgsConstructor
@Slf4j
public class DetectorManager {
    private static final String CK_DETECTOR_REFRESH_PERIOD = "detector-refresh-period";
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Getter
    @NonNull
    private DetectorSource detectorSource;

    private int detectorRefreshTimePeriod;

    private final Map<UUID, AnomalyDetector> cachedDetectors = new HashMap<>();

    public DetectorManager(DetectorSource detectorSource, Config config) {
        this.detectorSource = detectorSource;
        this.detectorRefreshTimePeriod = config.getInt(CK_DETECTOR_REFRESH_PERIOD);
        this.initScheduler();
    }

    private void initScheduler() {
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                this.detectorMapRefresh();
            } catch (Exception e) {
                log.error("Error refreshing detectors", e);
            }
        }, 1, detectorRefreshTimePeriod, TimeUnit.MINUTES);
    }

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

        val detectorUuid = mappedMetricData.getDetectorUuid();
        val metricDef = mappedMetricData.getMetricData().getMetricDefinition();

        AnomalyDetector detector = cachedDetectors.get(detectorUuid);
        if (detector == null) {
            detector = detectorSource.findDetector(detectorUuid, metricDef);
            cachedDetectors.put(detectorUuid, detector);
        }
        return detector;
    }

    /**
     * Remove detectors from cache that have been modified in last `timePeriod` minutes.
     * The deleted detectors will be cleaned up and the detectors modified will be reloaded
     * when corresponding mapped-metric comes in.
     */
    List<UUID> detectorMapRefresh() {

        List<UUID> updatedDetectors = new ArrayList<>();
        detectorSource.findUpdatedDetectors(detectorRefreshTimePeriod).forEach(key -> {
            updatedDetectors.add(key);
            cachedDetectors.remove(key);
        });

        log.info("Removed detectors on refresh : {}"+updatedDetectors);
        return updatedDetectors;
    }
}
