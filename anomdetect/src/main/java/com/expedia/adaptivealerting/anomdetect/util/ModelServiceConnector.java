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

import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.metrictank.MetricTankIdFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.fluent.Content;
import org.springframework.hateoas.Resources;

import java.io.IOException;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

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
@Slf4j
public class ModelServiceConnector {
    private final MetricTankIdFactory metricTankIdFactory = new MetricTankIdFactory();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Getter
    private HttpClientWrapper httpClient;
    
    @Getter
    private String uriTemplate;
    
    public ModelServiceConnector(HttpClientWrapper httpClient, String uriTemplate) {
        notNull(httpClient, "httpClient can't be null");
        notNull(uriTemplate, "uriTemplate can't be null");
        this.httpClient = httpClient;
        this.uriTemplate = uriTemplate;
    }
    
    public Resources<ModelResource> findModels(MetricDefinition metricDefinition) {
        notNull(metricDefinition, "metricDefinition can't be null");
        
        final String id = metricTankIdFactory.getId(metricDefinition);
        final String uri = String.format(uriTemplate, id);
        
        log.info("Finding models: metricDefinition={}, id={}, uri={}", metricDefinition, id, uri);
        
        try {
            final Content content = httpClient.get(uri);
            return objectMapper.readValue(content.asBytes(), ModelResources.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
