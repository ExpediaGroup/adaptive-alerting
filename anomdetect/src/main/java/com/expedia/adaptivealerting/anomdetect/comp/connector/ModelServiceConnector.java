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
package com.expedia.adaptivealerting.anomdetect.comp.connector;

import com.expedia.adaptivealerting.anomdetect.DetectorDeserializationException;
import com.expedia.adaptivealerting.anomdetect.DetectorException;
import com.expedia.adaptivealerting.anomdetect.DetectorMappingDeserializationException;
import com.expedia.adaptivealerting.anomdetect.DetectorMappingRetrievalException;
import com.expedia.adaptivealerting.anomdetect.DetectorNotFoundException;
import com.expedia.adaptivealerting.anomdetect.DetectorRetrievalException;
import com.expedia.adaptivealerting.anomdetect.detectormapper.DetectorMapper;
import com.expedia.adaptivealerting.anomdetect.detectormapper.DetectorMapping;
import com.expedia.adaptivealerting.anomdetect.detectormapper.DetectorMatchResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.client.fluent.Content;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isTrue;
import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

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
    public static final String API_PATH_MODEL_BY_DETECTOR_UUID = "/api/v2/detectors/findByUuid?uuid=%s";
    public static final String API_PATH_DETECTOR_UPDATES = "/api/v2/detectors/getLastUpdatedDetectors?interval=%d";
    public static final String API_PATH_DETECTOR_MAPPING_UPDATES = "/api/detectorMappings/lastUpdated?timeInSecs=%d";
    public static final String API_PATH_MATCHING_DETECTOR_BY_TAGS = "/api/detectorMappings/findMatchingByTags";

    private final HttpClientWrapper httpClient;
    private final String baseUri;
    private final ObjectMapper objectMapper;

    public ModelServiceConnector(HttpClientWrapper httpClient, String baseUri, ObjectMapper objectMapper) {
        notNull(httpClient, "httpClient can't be null");
        notNull(baseUri, "baseUri can't be null");
        notNull(objectMapper, "objectMapper can't be null");

        this.httpClient = httpClient;
        this.baseUri = baseUri;
        this.objectMapper = objectMapper;
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
    public DetectorResource findLatestDetector(UUID detectorUuid) {
        notNull(detectorUuid, "detectorUuid can't be null");

        // http://modelservice/api/models/search/findLatestByDetectorUuid?uuid=%s
        // http://modelservice/api/models/search/findLatestByDetectorUuid?uuid=85f395a2-e276-7cfd-34bc-cb850ae3bc2e
        val uri = String.format(baseUri + API_PATH_MODEL_BY_DETECTOR_UUID, detectorUuid);

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

        DetectorResource detectorResource;
        try {
            detectorResource = objectMapper.readValue(content.asBytes(), DetectorResource.class);
        } catch (IOException e) {
            val message = "IOException while deserializing models for detector " + detectorUuid;
            throw new DetectorDeserializationException(message, e);
        }

        if (detectorResource == null) {
            throw new DetectorNotFoundException("No models for detectorUuid=" + detectorUuid);
        }

        return detectorResource;
    }

    /**
     * @param sinceMinutes the time period in minutes
     * @return the list of detectorMappings that were modified in last since minutes
     */
    public DetectorResources findUpdatedDetectors(int sinceMinutes) {
        isTrue(sinceMinutes > 0, "timePeriod must be strictly positive");

        val uri = String.format(baseUri + API_PATH_DETECTOR_UPDATES, sinceMinutes);
        Content content;
        try {
            content = httpClient.get(uri);
        } catch (IOException e) {
            val message = "IOException while getting last updated detectors" +
                    ": timePeriod=" + sinceMinutes +
                    ", httpMethod=GET" +
                    ", uri=" + uri;
            throw new DetectorRetrievalException(message, e);
        }

        try {
            return objectMapper.readValue(content.asBytes(), DetectorResources.class);
        } catch (IOException e) {
            val message = "IOException while deserializing detectors" +
                    ": timePeriod=" + sinceMinutes;
            throw new DetectorDeserializationException(message, e);
        }
    }


    /**
     * Find matching detectors for a list of metrics, represented by a set of tags
     *
     * @param tagsList list of metric tags
     * @return the detector match response
     */
    public DetectorMatchResponse findMatchingDetectorMappings(List<Map<String, String>> tagsList) {
        isTrue(tagsList.size() > 0, "tagsList must not be empty");

        val uri = baseUri + API_PATH_MATCHING_DETECTOR_BY_TAGS;
        Content content;
        try {
            String body = objectMapper.writeValueAsString(tagsList);
            content = httpClient.post(uri, body);
        } catch (IOException e) {
            val message = "IOException while getting matching detectors for" +
                    ": tags=" + tagsList +
                    ", httpMethod=POST" +
                    ", uri=" + uri;
            throw new DetectorMappingRetrievalException(message, e);
        }
        try {
            return objectMapper.readValue(content.asBytes(), DetectorMatchResponse.class);
        } catch (IOException e) {
            val message = "IOException while deserializing detectorMatchResponse" +
                    ": tags=" + tagsList;
            throw new DetectorMappingDeserializationException(message, e);
        }

    }

    /**
     * Find updated detector mappings list.
     *
     * @param timeInSecs the time period in seconds
     * @return the list of detectormappings that were modified in last since minutes
     */
    public List<DetectorMapping> findUpdatedDetectorMappings(int timeInSecs) {
        // converting to seconds
        val uri = String.format(baseUri + API_PATH_DETECTOR_MAPPING_UPDATES, timeInSecs);
        Content content;
        try {
            content = httpClient.get(uri);
        } catch (IOException e) {
            val message = "IOException while getting last updated detectors mappings" +
                    ": timeInSecs=" + timeInSecs +
                    ", httpMethod=GET" +
                    ", uri=" + uri;
            throw new DetectorMappingRetrievalException(message, e);
        }

        try {
            List<DetectorMapping> result = objectMapper.readValue(content.asBytes(), new TypeReference<List<DetectorMapping>>() {
            });
            if (result == null) throw new IOException();
            return result;
        } catch (IOException e) {
            val message = "IOException while deserializing updated detectors mappings" +
                    ": timeInSecs=" + timeInSecs;
            throw new DetectorMappingDeserializationException(message, e);
        }
    }
}
