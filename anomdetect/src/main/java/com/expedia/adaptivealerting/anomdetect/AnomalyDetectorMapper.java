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
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.metrics.MetricData;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.stream.Collectors;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Entry into the Adaptive Alerting runtime. Its job is find for any incoming {@link MetricData} the corresponding set
 * of mapped detectors, creating a {@link MappedMetricData} for each.
 *
 * @author David Sutherland
 * @author Willie Wheeler
 */
@RequiredArgsConstructor
@Slf4j
public class AnomalyDetectorMapper {
    
    @Getter
    @NonNull
    private DetectorSource detectorSource;
    
    /**
     * Maps an {@link MetricData} to its corresponding set of {@link MappedMetricData}s.
     *
     * @param metricData MetricData to map.
     * @return The corresponding set of {@link MappedMetricData}s: one per detector.
     */
    public Set<MappedMetricData> map(MetricData metricData) {
        notNull(metricData, "metricData can't be null");
        return detectorSource
                .findDetectorMetas(metricData.getMetricDefinition())
                .stream()
                .map(meta -> new MappedMetricData(metricData, meta.getUuid(), meta.getType()))
                .collect(Collectors.toSet());
    }
}
