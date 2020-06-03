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

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.expedia.adaptivealerting.anomdetect.detect.AnomalyLevel;
import com.expedia.adaptivealerting.anomdetect.detect.Detector;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorContainer;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorResult;
import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;
import com.expedia.adaptivealerting.anomdetect.source.DetectorSource;
import com.expedia.adaptivealerting.anomdetect.source.data.initializer.DataInitializer;
import com.expedia.adaptivealerting.anomdetect.source.data.initializer.DetectorDataInitializationThrottledException;
import com.expedia.metrics.MetricData;
import com.typesafe.config.Config;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

/**
 * Component that manages a given set of anomaly detectors.
 * <p>
 * Detector manager maintains an internal cache of (UUID : Detectors).
 * This cache is kept up-to-date by polling modelservice for changes.
 * <p>
 * An alternative event-based approach to keep cache updated is to compare last-modified timestamp of a detector.
 * This approach however doesn't provide a way to delete an existing detector.
 */
@RequiredArgsConstructor
@Slf4j
// TODO: This class is getting much too big. Refactor by breaking out smaller, single-purpose collaborator classes.
public class DetectorManager {
    private static final String CK_DETECTOR_REFRESH_PERIOD = "detector-refresh-period";
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Timer detectorForTimer;
    private final Meter noDetectorFoundMeter;
    private final Function<String, Timer> detectTimer;
    private MetricRegistry metricRegistry;
    private final DetectorExecutorImpl detectorExecutor = new DetectorExecutorImpl();

    // TODO Consider making this an explicit class so we can mock it and verify interactions
    //  against it. [WLW]
    private final Map<UUID, DetectorContainer> cachedDetectors;
    @Getter
    @NonNull
    private DetectorSource detectorSource;
    private int detectorRefreshTimePeriod;

    //This assumes that we are running single thread per consumer
    private Set<UUID> detectorsLastUsedTimeToBeUpdatedSet;

    private long cacheSyncedTillTime = System.currentTimeMillis();
    private long detectorsLastUsedSyncedTillTime = System.currentTimeMillis();
    private DataInitializer dataInitializer;

    /**
     * Creates a new detector manager from the given parameters.
     *
     * @param detectorSource  DetectorSource
     * @param dataInitializer DataInitializer collaborator
     * @param config          Config
     * @param cachedDetectors Map containing cached detectors
     * @param metricRegistry  MetricRegistry collaborator
     */
    public DetectorManager(DetectorSource detectorSource,
                           DataInitializer dataInitializer,
                           Config config,
                           Map<UUID, DetectorContainer> cachedDetectors,
                           MetricRegistry metricRegistry) {
        // TODO: Seems odd to include this constructor, whose purpose seems to be to support unit testing.
        //  At least I don't think it should be public.
        //  This is conceptually just the base constructor with the cache exposed.[WLW]
        this.cachedDetectors = cachedDetectors;
        this.dataInitializer = dataInitializer;
        this.detectorSource = detectorSource;
        this.detectorRefreshTimePeriod = config.getInt(CK_DETECTOR_REFRESH_PERIOD);
        this.detectorsLastUsedTimeToBeUpdatedSet = new LinkedHashSet<>();

        this.metricRegistry = metricRegistry;
        detectorForTimer = metricRegistry.timer("detector.detectorFor");
        noDetectorFoundMeter = metricRegistry.meter("detector.nullDetector");
        detectTimer = (name) -> metricRegistry.timer("detector." + name + ".detect");

        this.initScheduler();
    }

    public DetectorManager(DetectorSource detectorSource,
                           DataInitializer dataInitializer,
                           Config config,
                           MetricRegistry metricRegistry) {
        this(detectorSource, dataInitializer, config, new HashMap<>(), metricRegistry);
    }

    private void initScheduler() {
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                log.trace("Refreshing detectors");
                this.detectorCacheSync(System.currentTimeMillis());
                this.detectorLastUsedTimeSync(System.currentTimeMillis());
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
    public DetectorResult detect(@NonNull MappedMetricData mappedMetricData) {
        try {
            MDC.put("DetectorUuid", mappedMetricData.getDetectorUuid().toString());
            checkMappedMetricData(mappedMetricData);
            Optional<DetectorContainer> container = getDetector(mappedMetricData);
            if (container.isPresent()) {
                Optional<DetectorResult> optionalDetectorResult = doDetection(container.get(), mappedMetricData.getMetricData());
                return optionalDetectorResult.orElse(null);
            } else {
                return null;
            }
        } finally {
            MDC.remove("DetectorUuid");
        }
    }

    private Optional<DetectorContainer> getDetector(MappedMetricData mappedMetricData) {
        Optional<DetectorContainer> optionalDetector = detectorFor(mappedMetricData);
        if (!optionalDetector.isPresent()) {
            log.warn("No detector for mappedMetricData={}", mappedMetricData);
            noDetectorFoundMeter.mark();
        }
        return optionalDetector;
    }

    private Optional<DetectorContainer> detectorFor(MappedMetricData mappedMetricData) {
        try (Timer.Context autoClosable = detectorForTimer.time()) {
            val detectorUuid = mappedMetricData.getDetectorUuid();
            DetectorContainer container = cachedDetectors.get(detectorUuid);
            detectorsLastUsedTimeToBeUpdatedSet.add(detectorUuid);
            if (container == null) {
                container = detectorSource.findDetector(detectorUuid);
                return (container == null) ? Optional.empty()
                        : initDataAndCacheIfSuccessful(mappedMetricData, detectorUuid, container);
            } else {
                log.trace("Got cached detector");
                return Optional.of(container);
            }
        }
    }

    private Optional<DetectorContainer> initDataAndCacheIfSuccessful(MappedMetricData mappedMetricData,
                                                                     UUID detectorUuid,
                                                                     DetectorContainer container) {
        // NPE HERE
        boolean dataInitCompleted = attemptDataInitialization(mappedMetricData, container.getDetector());
        if (dataInitCompleted) {
            cachedDetectors.put(detectorUuid, container);
            log.debug("Data Initialization phase is complete.  Caching detector.");
            return Optional.ofNullable(container);
        } else {
            log.debug("Data Initialization incomplete.  Discarding detector from memory to allow future re-attempts.");
            return Optional.empty();
        }
    }

    private boolean attemptDataInitialization(MappedMetricData mappedMetricData, Detector detector) {
        boolean dataInitCompleted;
        try {
            val detectorMapping = detectorSource.findDetectorMappingByUuid(detector.getUuid());
            dataInitializer.initializeDetector(mappedMetricData, detector, detectorMapping);
            dataInitCompleted = true;
        } catch (DetectorDataInitializationThrottledException e) {
            log.info("Data Initialization throttled: {}", e.getMessage());
            dataInitCompleted = false;
        } catch (Exception e) {
            log.error("Error encountered while initialising detector. Ignoring error and proceeding with un-initialized detector.", e);
            dataInitCompleted = true;
        }
        return dataInitCompleted;
    }

    private Optional<DetectorResult> doDetection(DetectorContainer container, MetricData metricData) {
        try (Timer.Context autoClosable = detectTimer.apply(container.getName()).time()) {
            Optional<DetectorResult> optionalDetectorResult = Optional.empty();
            try {
                DetectorResult detectorResult = detectorExecutor.doDetection(container, metricData);
                optionalDetectorResult = Optional.of(detectorResult);
            } catch (Exception e) {
                log.error("Error during anomaly detection", e);
            } finally {
                markAnomalyLevelMeter(container.getDetector(), optionalDetectorResult);
            }
            return optionalDetectorResult;
        }
    }

    private void markAnomalyLevelMeter(@NonNull Detector detector, Optional<DetectorResult> optionalDetectorResult) {
        AnomalyLevel anomalyLevel = optionalDetectorResult.map(DetectorResult::getAnomalyLevel).orElse(null);
        getDetectorAndLevelMeter(detector.getName(), anomalyLevel).mark();
    }

    public Meter getDetectorAndLevelMeter(String name, AnomalyLevel anomalyLevel) {
        String anomalyLevelStr = (anomalyLevel == null) ? "NONE" : anomalyLevel.name();
        return metricRegistry.meter("detector." + name + "." + anomalyLevelStr);
    }

    private void checkMappedMetricData(MappedMetricData mappedMetricData) {
        notNull(mappedMetricData.getMetricData(), "MappedMetricData contains illegal metricData=null");
        notNull(mappedMetricData.getDetectorUuid(), "MappedMetricData contains illegal detectorUuid=null");
    }

    /**
     * Sync with detector data store by polling to get all deleted detectors
     * The deleted detectors will be cleaned up and the detectors modified will be reloaded
     * when corresponding mapped-metric comes in.
     * On successful sync update cacheSyncedTillTime to currentTime
     */
    List<UUID> detectorCacheSync(long currentTime) {
        List<UUID> updatedDetectors = new ArrayList<>();
        long updateDurationInSeconds = (currentTime - cacheSyncedTillTime) / 1000;

        if (updateDurationInSeconds <= 0) {
            return updatedDetectors;
        }

        detectorSource.findUpdatedDetectors(updateDurationInSeconds).forEach(key -> {
            if (cachedDetectors.containsKey(key)) {
                cachedDetectors.remove(key);
                updatedDetectors.add(key);
            }
        });

        if (!updatedDetectors.isEmpty()) {
            log.info("Removed these updated detectors from cache (so they can be reloaded at time of next metric observation): {}", updatedDetectors);
        }
        cacheSyncedTillTime = currentTime;
        return updatedDetectors;
    }

    /**
     * Sync detector last used time to keep a track of detector usage.
     * On successful sync, update detectorLastUsedSyncedTillTime to currentTime
     */
    void detectorLastUsedTimeSync(long currentTime) {
        long updateDurationInSeconds = (currentTime - detectorsLastUsedSyncedTillTime) / 1000;

        if (updateDurationInSeconds <= 0 || detectorsLastUsedTimeToBeUpdatedSet.isEmpty()) {
            return;
        }

        log.info("Updating last used time for a total of {} invoked detectors", detectorsLastUsedTimeToBeUpdatedSet.size());
        processDetectorsLastUsedTimeSet();
        detectorsLastUsedSyncedTillTime = currentTime;
    }

    //Set.remove() during iteration throws a ConcurrentModificationException, so have to use Iterator.remove() instead. [KS]
    private void processDetectorsLastUsedTimeSet() {
        int counter = 0;
        for (Iterator<UUID> iterator = detectorsLastUsedTimeToBeUpdatedSet.iterator(); iterator.hasNext(); ) {
            UUID detectorUuid = iterator.next();
            detectorSource.updatedDetectorLastUsed(detectorUuid);
            iterator.remove();
            counter++;
        }

        log.info("Updated last used time for a total of {} invoked detectors", counter);
    }
}
