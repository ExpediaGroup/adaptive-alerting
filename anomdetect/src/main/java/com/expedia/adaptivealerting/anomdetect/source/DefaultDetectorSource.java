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

import com.expedia.adaptivealerting.anomdetect.AnomalyDetector;
import com.expedia.adaptivealerting.anomdetect.util.DetectorMeta;
import com.expedia.adaptivealerting.anomdetect.util.ModelServiceConnector;
import com.expedia.metrics.MetricDefinition;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * A {@link DetectorSource} backed by the Model Service.
 *
 * @author Willie Wheeler
 */
@RequiredArgsConstructor
public final class DefaultDetectorSource implements DetectorSource {
    
    @Getter
    @NonNull
    private ModelServiceConnector connector;
    
    @Override
    public List<DetectorMeta> findDetectorMetas(MetricDefinition metricDefinition) {
        notNull(metricDefinition, "metricDefinition can't be null");
        return connector
                .findDetectors(metricDefinition)
                .getContent()
                .stream()
                .map(resource -> new DetectorMeta(
                        UUID.fromString(resource.getUuid()),
                        resource.getType().getKey()))
                .collect(Collectors.toList());
    }
    
    @Override
    public AnomalyDetector findDetector(UUID detectorUuid) {
        notNull(detectorUuid, "detectorUUID can't be null");
        
        return null;
    }
}
