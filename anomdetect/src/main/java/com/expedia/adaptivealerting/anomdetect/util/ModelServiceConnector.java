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
import org.apache.http.client.fluent.Content;

import java.io.IOException;
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

    /**
     * Calls the Model Service to finds the detectors for the given metric definition.
     *
     * @param metricDefinition metric definition
     * @return detectors for the given metric definition
     * @throws DetectorRetrievalException       if there's a problem calling the Model Service
     * @throws DetectorDeserializationException if there's a problem deserializing the Model Service response into a
     *                                          detector list (e.g., invalid detector models)
     * @throws DetectorException                if there's any other problem finding the detector list
     */
    public DetectorResources findDetectors(MetricDefinition metricDefinition) {
        notNull(metricDefinition, "metricDefinition can't be null");

        val metricId = metricTankIdFactory.getId(metricDefinition);

        // http://modelservice/api/detectors/search/findByMetricHash?hash=%s
        // http://modelservice/api/detectors/search/findByMetricHash?hash=1.bbbad54f9232ba765e20368fe9c1a9c4
        val uri = String.format(uriTemplate, metricId);

        Content content;
        try {
            content = httpClient.get(uri);
        } catch (IOException e) {
            val message = "IOException while getting detectors" +
                    ": metricDefinition=" + metricDefinition +
                    ", metricId=" + metricId +
                    ", httpMethod=GET" +
                    ", uri=" + uri;
            throw new DetectorRetrievalException(message, e);
        }

        try {
            return objectMapper.readValue(content.asBytes(), DetectorResources.class);
        } catch (IOException e) {
            val message = "IOException while deserializing detectors" +
                    ": metricDefinition=" + metricDefinition;
            throw new DetectorDeserializationException(message, e);
        }
    }

    /**
     * Finds the latest model for the given detector.
     *
     * @param detectorUuid detector UUID
     * @return latest model for the given detector
     * @throws DetectorRetrievalException       if there's a problem calling the Model Service
     * @throws DetectorDeserializationException if there's a problem deserializing the Model Service response into a
     *                                          model (e.g., invalid model)
     * @throws DetectorNotFoundException        if the detector doesn't have any models
     * @throws DetectorException                if there's any other problem finding the detector
     */
    public ModelResource findLatestModel(UUID detectorUuid) {
        notNull(detectorUuid, "detectorUuid can't be null");

        // http://modelservice/api/models/search/findLatestByDetectorUuid?uuid=%s
        // http://modelservice/api/models/search/findLatestByDetectorUuid?uuid=85f395a2-e276-7cfd-34bc-cb850ae3bc2e
        val uri = String.format(uriTemplate, detectorUuid);

        // This returns a list, but it contains either a single detector or none.
        // We should have made the backing method return a Model instead of a List<Model>. [WLW]
        Content content;
        try {
            content = httpClient.get(uri);
        } catch (IOException e) {
            val message = "IOException while getting models for detector " + detectorUuid +
                    ": httpMethod=GET" +
                    ", uri=" + uri;
            throw new DetectorRetrievalException(message, e);
        }

        ModelResources modelResources;
        try {
            modelResources = objectMapper.readValue(content.asBytes(), ModelResources.class);
        } catch (IOException e) {
            val message = "IOException while deserializing models for detector " + detectorUuid;
            throw new DetectorDeserializationException(message, e);
        }

        val modelResourceList = modelResources.getEmbedded().getModels();
        if (modelResourceList.isEmpty()) {
            throw new DetectorNotFoundException("No models for detectorUuid=" + detectorUuid);
        }

        return modelResourceList.get(0);
    }
}
