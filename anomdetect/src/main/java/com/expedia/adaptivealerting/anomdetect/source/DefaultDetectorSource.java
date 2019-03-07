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

import com.expedia.adaptivealerting.anomdetect.AbstractAnomalyDetector;
import com.expedia.adaptivealerting.anomdetect.AnomalyDetector;
import com.expedia.adaptivealerting.anomdetect.DetectorLookup;
import com.expedia.adaptivealerting.anomdetect.DetectorParams;
import com.expedia.adaptivealerting.anomdetect.util.DetectorMeta;
import com.expedia.adaptivealerting.anomdetect.util.ModelServiceConnector;
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
    public List<DetectorMeta> findDetectorMetas(MetricDefinition metricDef) {
        notNull(metricDef, "metricDefinition can't be null");
        return connector
                .findDetectors(metricDef)
                .getContent()
                .stream()
                .map(resource -> new DetectorMeta(
                        UUID.fromString(resource.getUuid()),
                        resource.getType().getKey()))
                .collect(Collectors.toList());
    }
    
    @Override
    public AnomalyDetector findDetector(DetectorMeta detectorMeta, MetricDefinition metricDef) {
        notNull(detectorMeta, "detectorMeta can't be null");

        // metricDef _can_ be null, and normally is.
        // This implementation doesn't use it, but other implementations do.
        
        val detectorUuid = detectorMeta.getUuid();

        // TODO "Latest model" doesn't really make sense for the kind of detectors we load into the DetectorManager.
        // These are basic detectors backed by single statistical models, as opposed to being ML models that we have to
        // refresh/retrain periodically. So we probably want to simplify this by just collapsing the model concept into
        // the detector. [WLW]
        val model = connector.findLatestModel(detectorUuid);
    
        if (model == null) {
            log.error("No detector for detectorUuid={}", detectorUuid);
            // TODO Is this how we want to handle this? [WLW]
            return null;
        }

        val detectorType = detectorMeta.getType();
        val detectorClass = detectorLookup.getDetector(detectorType);
        val detector = (AbstractAnomalyDetector) ReflectionUtil.newInstance(detectorClass);
        val paramsClass = detector.getParamsClass();
        val params = (DetectorParams) new ObjectMapper().convertValue(model.getParams(), paramsClass);

        detector.init(detectorUuid, params);
        log.info("Found detector: {}", detector);
        return detector;
    }
}
