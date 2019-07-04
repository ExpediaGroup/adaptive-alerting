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

import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;
import com.expedia.adaptivealerting.anomdetect.detect.Detector;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorResult;
import com.expedia.adaptivealerting.anomdetect.source.DetectorSource;
import com.typesafe.config.Config;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import lombok.var;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

/**
 * Component that manages a given set of anomaly detectors.
 * <p>
 * Detector manager maintains an internal cache of (UUID : Detectors).
 * This cache is kept up-to-date by polling modelservice for changes.
 * <p>
 * An alternative event-based approach to keep cache updated is to compare last-modified timestamp of a detector.
 * This approach however doesn't provide a way to delete an existing detector .
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

    // TODO Consider making this an explicit class so we can mock it and verify interactions
    //  against it. [WLW]
    private final Map<UUID, Detector> cachedDetectors;
    private long synchedTilTime = System.currentTimeMillis();

    // TODO Seems odd to include this constructor, whose purpose seems to be to support unit
    //  testing. At least I don't think it should be public. And it should take a Config
    //  since the other one does, and this is conceptually just the base constructor with
    //  the cache exposed.[WLW]
    public DetectorManager(DetectorSource detectorSource, int detectorRefreshTimePeriod, Map<UUID, Detector> cachedDetectors) {
        this.detectorSource = detectorSource;
        this.detectorRefreshTimePeriod = detectorRefreshTimePeriod;
        this.cachedDetectors = cachedDetectors;
        this.initScheduler();
    }

    public DetectorManager(DetectorSource detectorSource, Config config) {
        this(detectorSource, config.getInt(CK_DETECTOR_REFRESH_PERIOD), new HashMap<>());
    }

    private void initScheduler() {
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                log.trace("Refreshing detectors");
                this.detectorCacheSync(System.currentTimeMillis());
            } catch (Exception e) {
                log.error("Error refreshing detectors", e);
            }
        }, detectorRefreshTimePeriod, detectorRefreshTimePeriod, TimeUnit.MINUTES);
    }

    /**
     * Classifies the mapped metric data, performing detector lookup behind the scenes. Returns {@code null} if there's
     * no detector defined for the given mapped metric data.
     *
     * @param mappedMetricData Mapped metric data.
     * @return The anomaly result, or {@code null} if there's no associated detector.
     */
    public DetectorResult detect(MappedMetricData mappedMetricData) {
        notNull(mappedMetricData, "mappedMetricData can't be null");

        val detector = detectorFor(mappedMetricData);
        if (detector == null) {
            log.warn("No detector for mappedMetricData={}", mappedMetricData);
            return null;
        }
        val metricData = mappedMetricData.getMetricData();
        return detector.detect(metricData);
    }

    private Detector detectorFor(MappedMetricData mappedMetricData) {
        notNull(mappedMetricData, "mappedMetricData can't be null");

        val detectorUuid = mappedMetricData.getDetectorUuid();
        var detector = cachedDetectors.get(detectorUuid);
        if (detector == null) {
            detector = detectorSource.findDetector(detectorUuid);
            cachedDetectors.put(detectorUuid, detector);
        } else {
            log.trace("Got cached detector");
        }
        return detector;
    }

    /**
     * Sync with detector datastore by polling to get all deleted detectors
     * The deleted detectors will be cleaned up and the detectors modified will be reloaded
     * when corresponding mapped-metric comes in.
     * On successful sync update syncUptill time to currentTime
     */
    List<UUID> detectorCacheSync(long currentTime) {
        var updatedDetectors = new ArrayList<UUID>();

        long updateDurationInSeconds = (currentTime - synchedTilTime) / 1000;

        if (updateDurationInSeconds <= 0) {
            return updatedDetectors;
        }

        detectorSource.findUpdatedDetectors(updateDurationInSeconds).forEach(key -> {
            if (cachedDetectors.containsKey(key)) {
                cachedDetectors.remove(key);
                updatedDetectors.add(key);
            }
        });

        log.info("Removed detectors on refresh : {}", updatedDetectors);
        synchedTilTime = currentTime;
        return updatedDetectors;
    }
}
