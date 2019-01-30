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
import com.expedia.adaptivealerting.anomdetect.ewma.EwmaAnomalyDetector;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.metrictank.MetricTankIdFactory;
import lombok.val;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * A basic {@link DetectorSource} implementation that maps every metric to a single EWMA detector, managed in-memory.
 *
 * @author Willie Wheeler
 */
public final class BasicDetectorSource implements DetectorSource {
    private static final String EWMA = "ewma-detector";
    
    private final MetricTankIdFactory idFactory = new MetricTankIdFactory();
    private final Map<UUID, AnomalyDetector> detectorMap = new HashMap<>();
    
    @Override
    public List<DetectorMeta> findDetectorMetas(MetricDefinition metricDefinition) {
        notNull(metricDefinition, "metricDefinition can't be null");
        val metricId = idFactory.getId(metricDefinition);
        val detectorUuid = UUID.nameUUIDFromBytes(metricId.getBytes());
        val detectorMeta = new DetectorMeta(detectorUuid, EWMA);
        return Collections.singletonList(detectorMeta);
    }
    
    @Override
    public AnomalyDetector findDetector(UUID detectorUuid) {
        notNull(detectorUuid, "detectorUUID can't be null");
        AnomalyDetector detector = detectorMap.get(detectorUuid);
        if (detector == null) {
            detector = new EwmaAnomalyDetector(detectorUuid);
            detectorMap.put(detectorUuid, detector);
        }
        return detector;
    }
}
