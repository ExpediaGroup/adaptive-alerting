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
import com.expedia.metrics.MetricDefinition;
import lombok.val;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

public class MultiDetectorSource implements DetectorSource {
    private final List<DetectorSource> detectorSources;
    
    public MultiDetectorSource(List<DetectorSource> detectorSources) {
        notNull(detectorSources, "detectorSources can't be null");
        this.detectorSources = detectorSources;
    }
    
    /**
     * Returns the first non-empty meta list, or the empty meta list if they're all empty.
     *
     * @param metricDefinition The metric.
     * @return
     */
    @Override
    public List<DetectorMeta> findDetectorMetas(MetricDefinition metricDefinition) {
        notNull(metricDefinition, "metricDefinition can't be null");
        
        for (val detectorSource : detectorSources) {
            val detectorMetas = detectorSource.findDetectorMetas(metricDefinition);
            if (!detectorMetas.isEmpty()) {
                return detectorMetas;
            }
        }
        
        return Collections.EMPTY_LIST;
    }
    
    @Override
    public AnomalyDetector findDetector(UUID detectorUuid) {
        notNull(detectorUuid, "detectorUuid can't be null");
        
        for (val detectorSource : detectorSources) {
            val detector = detectorSource.findDetector(detectorUuid);
            if (detector != null) {
                return detector;
            }
        }
        
        return null;
    }
}
