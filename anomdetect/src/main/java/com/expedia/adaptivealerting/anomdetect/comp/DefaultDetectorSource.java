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
import com.expedia.adaptivealerting.anomdetect.comp.legacy.LegacyDetectorFactory;
import com.expedia.adaptivealerting.anomdetect.detector.Detector;
import com.expedia.adaptivealerting.anomdetect.mapper.DetectorMatchResponse;
import com.expedia.metrics.MetricDefinition;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.*;
import java.util.stream.Collectors;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

// Non-final because we currently need to mock this in DetectorManagerTest. [WLW]

/**
 * A {@link DetectorSource} backed by the Model Service.
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultDetectorSource implements DetectorSource {

    @NonNull
    private final ModelServiceConnector connector;

    @NonNull
    private final LegacyDetectorFactory legacyDetectorFactory;

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

        // TODO "Latest model" doesn't really make sense for the kind of detectors we load into the DetectorManager.
        //  These are basic detectors backed by single statistical models, as opposed to being ML models that we have to
        //  refresh/retrain periodically. So we probably want to simplify this by just collapsing the model concept into
        //  the detector. [WLW]
        val modelResource = connector.findLatestModel(uuid);

        return legacyDetectorFactory.createDetector(uuid, modelResource);
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
    @Override
    public DetectorMatchResponse findMatchingDetectorMappings(List<Map<String, String>> metricTags){
        return connector.findMatchingDetectorMappings(metricTags);
    }
}
