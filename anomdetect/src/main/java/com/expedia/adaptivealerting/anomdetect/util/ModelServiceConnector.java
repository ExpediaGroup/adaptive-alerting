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
package com.expedia.adaptivealerting.anomdetect.util;

import com.expedia.adaptivealerting.anomdetect.DetectorMapper;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.metrictank.MetricTankIdFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

// FIXME Currently this class uses the URI template in an inconsistent way. In some methods it fills in a detector UUID
// whereas in others it fills in a metric ID. The only reason it currently works is that no existing client uses both
// types of method. However if some client used both then the connector would break. We probably want to construct the
// URI from a scheme/host/port. (Ideal would be hypermedia, but might be overkill for this.) [WLW]

/**
 * <p>
 * Connector for interacting with the Model Service.
 * </p>
 * <p>
 * For now this is just part of the
 * {@link com.expedia.adaptivealerting.anomdetect} package as the only thing using it is the
 * {@link DetectorMapper}. If we find others needing to use it then we
 * might end up moving it into some common location.
 * </p>
 */
@Slf4j
public class ModelServiceConnector {
    private final MetricTankIdFactory metricTankIdFactory = new MetricTankIdFactory();
    
    // https://hdpe.me/post/spring-data-rest-hal-client/
    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json()
            .modules(new Jackson2HalModule())
            .build();
    
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
    
    public Resources<DetectorResource> findDetectors(MetricDefinition metricDefinition) throws IOException {
        notNull(metricDefinition, "metricDefinition can't be null");
        val metricId = metricTankIdFactory.getId(metricDefinition);
        val findDetectorsUri = String.format(uriTemplate, metricId);
        val content = httpClient.get(findDetectorsUri);
        return objectMapper.readValue(content.asBytes(), DetectorResources.class);
    }

    public ModelResource findLatestModel(UUID detectorUuid) throws IOException {
        notNull(detectorUuid, "detectorUuid can't be null");
        val resources = findModels(detectorUuid);
        val content = resources.getContent();
        val list = new ArrayList<>(content);
        return list.isEmpty() ? null : list.get(0);
    }
    
    private Resources<ModelResource> findModels(UUID detectorUuid) throws IOException {
        val findModelsUri = String.format(uriTemplate, detectorUuid);
        val content = httpClient.get(findModelsUri);
        return objectMapper.readValue(content.asBytes(), ModelResources.class);
    }
}
