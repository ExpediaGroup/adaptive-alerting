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

import com.expedia.adaptivealerting.anomdetect.util.DetectorResource;
import com.expedia.adaptivealerting.anomdetect.util.ModelServiceConnector;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Resources;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

// TODO Cache ModelService responses [WLW]

/**
 * Entry into the Adaptive Alerting runtime. Its job is find for any incoming {@link MetricData} the corresponding set
 * of mapped detectors, creating a {@link MappedMetricData} for each.
 *
 * @author David Sutherland
 * @author Willie Wheeler
 */
@Slf4j
public class AnomalyDetectorMapper {
    
    @Getter
    private ModelServiceConnector modelServiceConnector;
    
    /**
     * Creates a new mapper.
     *
     * @param modelServiceConnector Model service connector.
     */
    public AnomalyDetectorMapper(ModelServiceConnector modelServiceConnector) {
        notNull(modelServiceConnector, "modelServiceConnector can't be null");
        this.modelServiceConnector = modelServiceConnector;
    }
    
    /**
     * Maps an {@link MetricData} to its corresponding set of {@link MappedMetricData}s.
     *
     * @param metricData MetricData to map.
     * @return The corresponding set of {@link MappedMetricData}s: one per detector.
     */
    public Set<MappedMetricData> map(MetricData metricData) {
        notNull(metricData, "metricData can't be null");
        
        final MetricDefinition metricDefinition = metricData.getMetricDefinition();
        final Resources<DetectorResource> detectorResources = modelServiceConnector.findDetectors(metricDefinition);
        final Collection<DetectorResource> detectorCollection = detectorResources.getContent();
        
        // TODO This logging is expensive. Don't want to keep it permanently, at least not at INFO level. [WLW]
        log.info("metricData={}, models={}", metricData, Arrays.toString(detectorCollection.toArray()));
        
        return detectorCollection.stream()
                .map(model -> {
                    final UUID detectorUuid = UUID.fromString(model.getUuid());
                    final String detectorType = model.getType().getKey();
                    return new MappedMetricData(metricData, detectorUuid, detectorType);
                })
                .collect(Collectors.toSet());
    }
}
