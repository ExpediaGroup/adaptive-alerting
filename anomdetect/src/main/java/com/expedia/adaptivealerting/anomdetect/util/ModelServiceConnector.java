/*
 * Copyright 2018 Expedia Group, Inc.
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
package com.expedia.adaptivealerting.anomdetect.util;

import com.expedia.adaptivealerting.anomdetect.AnomalyDetectorMeta;
import com.expedia.metrics.MetricDefinition;
import lombok.Getter;
import sun.net.www.http.HttpClient;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * Connector for interacting with the Model Service.
 * </p>
 * <p>
 * For now this is just part of the
 * {@link com.expedia.adaptivealerting.anomdetect} package as the only thing using it is the
 * {@link com.expedia.adaptivealerting.anomdetect.AnomalyDetectorMapper}. If we find others needing to use it then we
 * might end up moving it into some common location.
 * </p>
 *
 * @author Willie Wheeler
 */
public class ModelServiceConnector {
    
    @Getter
    private HttpClient httpClient;
    
    public ModelServiceConnector(HttpClient httpClient) {
        this.httpClient = httpClient;
    }
    
    public Set<AnomalyDetectorMeta> findDetectors(MetricDefinition metricDefinition) {
//        MetricTankIdFactory idFactory = new MetricTankIdFactory();
//        String id = idFactory.getId(metricPoint.getMetricDefinition());
        
        final Set<AnomalyDetectorMeta> metas = new HashSet<>();
        return metas;
    }
}
