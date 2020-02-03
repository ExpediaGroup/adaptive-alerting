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
import com.expedia.adaptivealerting.anomdetect.detect.DetectorResult;
import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;
import com.expedia.adaptivealerting.anomdetect.source.DetectorSource;
import com.expedia.adaptivealerting.anomdetect.source.data.initializer.DataInitializer;
import com.typesafe.config.Config;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

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
    private final Timer detectorForTimer;
    private final Meter noDetectorFoundMeter;
    private final Function<String, Timer> detectTimer;
    private final BiFunction<String, AnomalyLevel, Meter> detectorAnomalyLevelMeter;

    // TODO Consider making this an explicit class so we can mock it and verify interactions
    //  against it. [WLW]
    private final Map<UUID, Detector> cachedDetectors;
    @Getter
    @NonNull
    private DetectorSource detectorSource;
    private int detectorRefreshTimePeriod;
    private long synchedTilTime = System.currentTimeMillis();
    private DataInitializer dataInitializer;


    /**
     * Creates a new detector manager from the given parameters.
     *
     * @param detectorSource  detector source
     * @param metricRegistry  metric registry
     * @param cachedDetectors map containing cached detectors
     */
    public DetectorManager(DetectorSource detectorSource, DataInitializer dataInitializer, Config config, Map<UUID, Detector> cachedDetectors,
                           MetricRegistry metricRegistry) {
        // TODO Seems odd to include this constructor, whose purpose seems to be to support unit
        //  testing. At least I don't think it should be public. And it should take a Config
        //  since the other one does, and this is conceptually just the base constructor with
        //  the cache exposed.[WLW]
        this.detectorSource = detectorSource;
        this.detectorRefreshTimePeriod = config.getInt(CK_DETECTOR_REFRESH_PERIOD);
        this.cachedDetectors = cachedDetectors;
        this.dataInitializer = dataInitializer;

        detectorForTimer = metricRegistry.timer("detector.detectorFor");
        noDetectorFoundMeter = metricRegistry.meter("detector.nullDetector");
        detectTimer = (name) -> metricRegistry.timer("detector." + name + ".detect");
        detectorAnomalyLevelMeter = (name, al) -> metricRegistry.meter("detector." + name + "." + al.name());

        this.initScheduler();
    }

    public DetectorManager(DetectorSource detectorSource, DataInitializer dataInitializer, Config config, MetricRegistry metricRegistry) {
        this(detectorSource, dataInitializer, config, new HashMap<>(), metricRegistry);
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
        MDC.put("DetectorUuid", mappedMetricData.getDetectorUuid().toString());
        try {
            //timer ...
            Timer.Context ctxt = detectorForTimer.time();
            val detector = detectorFor(mappedMetricData);
            ctxt.close();
            if (detector == null) {
                log.warn("No detector for mappedMetricData={}", mappedMetricData);
                noDetectorFoundMeter.mark();
                return null;
            }
            val metricData = mappedMetricData.getMetricData();
            DetectorResult result = null;
            try {
                ctxt = detectTimer.apply(detector.getName()).time();
                result = detector.detect(metricData);
                ctxt.close();
            } catch (Exception e) {
                log.error("Error in detector.detect", e);
            }

            detectorAnomalyLevelMeter.apply(detector.getName(), result.getAnomalyLevel()).mark();
            return result;
        } finally {
            MDC.remove("DetectorUuid");
        }
    }

    private Detector detectorFor(MappedMetricData mappedMetricData) {
        notNull(mappedMetricData, "mappedMetricData can't be null");
        val detectorUuid = mappedMetricData.getDetectorUuid();
        Detector detector = cachedDetectors.get(detectorUuid);
        if (detector == null) {
            detector = detectorSource.findDetector(detectorUuid);
            try {
                dataInitializer.initializeDetector(mappedMetricData, detector);
            } catch (Exception e) {
                log.error("Error encountered while initialising detector. Ignoring error and proceeding with un-initialized detector.", e);
            }
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
        List<UUID> updatedDetectors = new ArrayList<>();

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

        if (!updatedDetectors.isEmpty()) {
            log.info("Removed these updated detectors from cache (so they can be reloaded at time of next metric observation): {}", updatedDetectors);
        }
        synchedTilTime = currentTime;
        return updatedDetectors;
    }
}
