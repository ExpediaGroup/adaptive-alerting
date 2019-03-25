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

import com.expedia.adaptivealerting.anomdetect.comp.connector.ModelServiceConnector;
import com.expedia.adaptivealerting.anomdetect.detector.ConstantThresholdParams;
import com.expedia.adaptivealerting.anomdetect.detector.CusumParams;
import com.expedia.adaptivealerting.anomdetect.detector.Detector;
import com.expedia.adaptivealerting.anomdetect.detector.DetectorParams;
import com.expedia.adaptivealerting.core.anomaly.AnomalyType;
import com.expedia.adaptivealerting.core.util.ReflectionUtil;
import com.expedia.metrics.MetricDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

// Non-final because we currently need to mock this in DetectorManagerTest. [WLW]

/**
 * A {@link DetectorSource} backed by the Model Service.
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultDetectorSource implements DetectorSource {
    private final DetectorLookup detectorLookup = new DetectorLookup();

    @NonNull
    private final ModelServiceConnector connector;

    @Override
    public Set<String> findDetectorTypes() {
        return detectorLookup.getDetectorTypes();
    }

    @Override
    public List<UUID> findDetectorUuids(MetricDefinition metricDef) {
        notNull(metricDef, "metricDefinition can't be null");
        return connector
                .findDetectors(metricDef)
                .getEmbedded()
                .getDetectors()
                .stream()
                .map(resource -> UUID.fromString(resource.getUuid()))
                .collect(Collectors.toList());
    }

    @Override
    public Detector findDetector(UUID uuid) {
        notNull(uuid, "uuid can't be null");

        // TODO Currently we use a legacy process to find the detector. The legacy process couples point forecast algos
        //  with interval forecast algos. We will decouple these shortly. [WLW]
        return doLegacyFindDetector(uuid);
    }

    @Override
    public List<UUID> findUpdatedDetectors(int timePeriod) {
        notNull(timePeriod, "timePeriod can't be null");

        return connector
                .findUpdatedDetectors(timePeriod)
                .getEmbedded()
                .getDetectors()
                .stream()
                .map(resource -> UUID.fromString(resource.getUuid()))
                .collect(Collectors.toList());
    }


    // ================================================================================
    // Legacy
    // ================================================================================

    @Deprecated
    private Detector doLegacyFindDetector(UUID uuid) {

        // TODO "Latest model" doesn't really make sense for the kind of detectors we load into the DetectorManager.
        //  These are basic detectors backed by single statistical models, as opposed to being ML models that we have to
        //  refresh/retrain periodically. So we probably want to simplify this by just collapsing the model concept into
        //  the detector. [WLW]
        val model = connector.findLatestModel(uuid);

        val detectorType = model.getDetectorType().getKey();
        val detectorClass = detectorLookup.getDetector(detectorType);
        val detector = ReflectionUtil.newInstance(detectorClass);
        val paramsClass = detector.getParamsClass();
        val params = (DetectorParams) new ObjectMapper().convertValue(model.getParams(), paramsClass);
        val anomalyType = doLegacyGetAnomalyType(params);

        detector.init(uuid, params, anomalyType);
        log.info("Found detector: {}", detector);
        return detector;
    }

    @Deprecated
    private AnomalyType doLegacyGetAnomalyType(DetectorParams params) {
        val paramsClass = params.getClass();

        // TODO For now we simply reproduce current behavior, which is that only certain detectors support tails. Soon
        //  we'll remove these hardcodes since all detectors will support tails.
        if (ConstantThresholdParams.class.equals(paramsClass)) {
            return ((ConstantThresholdParams) params).getType();
        } else if (CusumParams.class.equals(paramsClass)) {
            return ((CusumParams) params).getType();
        } else {
            return AnomalyType.TWO_TAILED;
        }
    }
}
