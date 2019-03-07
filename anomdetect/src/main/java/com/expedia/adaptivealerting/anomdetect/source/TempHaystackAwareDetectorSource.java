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
import com.expedia.adaptivealerting.anomdetect.ewma.EwmaParams;
import com.expedia.adaptivealerting.anomdetect.util.DetectorMeta;
import com.expedia.adaptivealerting.anomdetect.ewma.EwmaAnomalyDetector;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.metrictank.MetricTankIdFactory;
import lombok.val;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * A temporary {@link DetectorSource} that we're using to get AA/Haystack integration working. This data source tries to
 * find detectors from a primary detector source (think {@link DefaultDetectorSource}), and if we don't find a detector
 * there, then we just create them dynamically and manage them in memory.
 *
 * @deprecated Don't want Haystack-specific code, but doing this just to get things started.
 */
public final class TempHaystackAwareDetectorSource implements DetectorSource {
    private static final String EWMA = "ewma-detector";
    private static final String PRODUCT = "product";
    private static final String HAYSTACK = "haystack";
    
    private final DetectorSource primaryDetectorSource;
    private final MetricTankIdFactory idFactory = new MetricTankIdFactory();
    private final Map<UUID, AnomalyDetector> haystackDetectorMap = new HashMap<>();
    
    public TempHaystackAwareDetectorSource(DetectorSource primaryDetectorSource) {
        notNull(primaryDetectorSource, "primaryDetectorSource can't be null");
        this.primaryDetectorSource = primaryDetectorSource;
    }
    
    @Override
    public Set<String> findDetectorTypes() {
        return primaryDetectorSource.findDetectorTypes();
    }
    
    @Override
    public List<DetectorMeta> findDetectorMetas(MetricDefinition metricDef) {
        notNull(metricDef, "metricDefinition can't be null");
        
        val detectorMetas = primaryDetectorSource.findDetectorMetas(metricDef);
        
        if (detectorMetas.size() > 0) {
            return detectorMetas;
        }
        
        return isHaystackMetric(metricDef) ?
                findHaystackDetectorMetas(metricDef) :
                Collections.EMPTY_LIST;
    }
    
    @Override
    public AnomalyDetector findDetector(DetectorMeta detectorMeta, MetricDefinition metricDef) {
        notNull(detectorMeta, "detectorMeta can't be null");
        
        val detector = primaryDetectorSource.findDetector(detectorMeta, metricDef);
        
        if (detector != null) {
            return detector;
        }
        
        return isHaystackMetric(metricDef) ?
                createHaystackDetector(detectorMeta.getUuid()) :
                null;
    }
    
    private boolean isHaystackMetric(MetricDefinition metricDef) {
        if (metricDef == null) {
            return false;
        }
        
        return HAYSTACK.equals(metricDef.getTags().getKv().get(PRODUCT));
    }
    
    private List<DetectorMeta> findHaystackDetectorMetas(MetricDefinition metricDef) {
        val metricId = idFactory.getId(metricDef);
        val detectorUuid = UUID.nameUUIDFromBytes(metricId.getBytes());
        val detectorMeta = new DetectorMeta(detectorUuid, EWMA);
        return Collections.singletonList(detectorMeta);
    }
    
    private AnomalyDetector createHaystackDetector(UUID detectorUuid) {
        AnomalyDetector detector = haystackDetectorMap.get(detectorUuid);
        if (detector == null) {
            detector = new EwmaAnomalyDetector();
            ((EwmaAnomalyDetector) detector).init(detectorUuid, new EwmaParams());
            haystackDetectorMap.put(detectorUuid, detector);
        }
        return detector;
    }
}
