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

import com.expedia.adaptivealerting.anomdetect.mapper.DetectorMapper;
import com.expedia.adaptivealerting.anomdetect.mapper.DetectorMapping;
import com.expedia.adaptivealerting.anomdetect.mapper.DetectorMatchResponse;
import com.expedia.adaptivealerting.anomdetect.util.HttpClientWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.client.fluent.Content;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isTrue;
import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

// FIXME Currently this class uses the URI template in an inconsistent way. In some methods it fills in a detector UUID
//  whereas in others it fills in a metric ID. The only reason it currently works is that no existing client uses both
//  types of method. However if some client used both then the connector would break. We probably want to construct the
//  URI from a scheme/host/port. (Ideal would be hypermedia, but might be overkill for this.) [WLW]

/**
 * <p>
 * Connector for interacting with the Model Service. This allows the anomaly detection module to load detector documents
 * and detector mappings from the Model Service (Elasticsearch) backend.
 * </p>
 * <p>
 * For now this is just part of the {@link com.expedia.adaptivealerting.anomdetect} package as the only thing using it
 * is the {@link DetectorMapper}. If we find others needing to use it then we might end up moving it into some common
 * location.
 * </p>
 */
@Slf4j
public class DetectorClient {
    public static final String API_PATH_MODEL_BY_DETECTOR_UUID = "/api/v2/detectors/findByUuid?uuid=%s";
    public static final String API_PATH_DETECTOR_UPDATES = "/api/v2/detectors/getLastUpdatedDetectors?interval=%d";

    // TODO Shouldn't these also include the /api/v2 prefix? [WLW]
    public static final String API_PATH_DETECTOR_MAPPING_UPDATES = "/api/detectorMappings/lastUpdated?timeInSecs=%d";
    public static final String API_PATH_MATCHING_DETECTOR_BY_TAGS = "/api/detectorMappings/findMatchingByTags";

    private final HttpClientWrapper httpClient;
    private final String baseUri;
    private final ObjectMapper objectMapper;

    public DetectorClient(HttpClientWrapper httpClient, String baseUri, ObjectMapper objectMapper) {
        notNull(httpClient, "httpClient can't be null");
        notNull(baseUri, "baseUri can't be null");
        notNull(objectMapper, "objectMapper can't be null");

        this.httpClient = httpClient;
        this.baseUri = baseUri;
        this.objectMapper = objectMapper;
    }

    /**
     * Finds the detector document for the given detector UUID.
     *
     * @param uuid detector UUID
     * @return latest model for the given detector
     * @throws DetectorException if there's a problem finding the detector document
     */
    public DetectorDocument findDetectorDocument(UUID uuid) {
        notNull(uuid, "uuid can't be null");

        // http://modelservice/api/v2/detectors/findByUuid?uuid=%s
        // http://modelservice/api/v2/detectors/findByUuid?uuid=85f395a2-e276-7cfd-34bc-cb850ae3bc2e
        val uri = String.format(baseUri + API_PATH_MODEL_BY_DETECTOR_UUID, uuid);

        Content content;
        try {
            content = httpClient.get(uri);
        } catch (IOException e) {
            val message = "IOException while getting detector document " + uuid +
                    ": httpMethod=GET" +
                    ", uri=" + uri;
            throw new DetectorException(message, e);
        }

        DetectorDocument document;
        try {
            document = objectMapper.readValue(content.asBytes(), DetectorDocument.class);
        } catch (IOException e) {
            val message = "IOException while reading detector document " + uuid;
            throw new DetectorException(message, e);
        }

        if (document == null) {
            throw new DetectorException("No detector document for uuid=" + uuid);
        }

        return document;
    }

    /**
     * @param sinceSeconds the time period in seconds
     * @return the list of detectorMappings that were modified in last since minutes
     */
    public List<DetectorDocument> findUpdatedDetectorDocuments(long sinceSeconds) {
        isTrue(sinceSeconds > 0, "sinceSeconds must be strictly positive");

        val uri = String.format(baseUri + API_PATH_DETECTOR_UPDATES, sinceSeconds);
        Content content;
        try {
            content = httpClient.get(uri);
        } catch (IOException e) {
            val message = "IOException while getting last updated detectors" +
                    ": sinceSeconds=" + sinceSeconds +
                    ", httpMethod=GET" +
                    ", uri=" + uri;
            throw new DetectorException(message, e);
        }

        try {
            return Arrays.asList(objectMapper.readValue(content.asBytes(), DetectorDocument[].class));
        } catch (IOException e) {
            val message = "IOException while reading detectors: sinceSeconds=" + sinceSeconds;
            throw new DetectorException(message, e);
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
            throw new DetectorException(message, e);
        }
        try {
            return objectMapper.readValue(content.asBytes(), DetectorMatchResponse.class);
        } catch (IOException e) {
            val message = "IOException while reading detectorMatchResponse: tags=" + tagsList;
            throw new DetectorException(message, e);
        }

    }

    /**
     * Find updated detector mappings list.
     *
     * @param timeInSecs the time period in seconds
     * @return the list of detectormappings that were modified in last since minutes
     */
    public List<DetectorMapping> findUpdatedDetectorMappings(long timeInSecs) {
        val uri = String.format(baseUri + API_PATH_DETECTOR_MAPPING_UPDATES, timeInSecs);
        Content content;
        try {
            content = httpClient.get(uri);
        } catch (IOException e) {
            val message = "IOException while getting last updated detectors mappings" +
                    ": timeInSecs=" + timeInSecs +
                    ", httpMethod=GET" +
                    ", uri=" + uri;
            throw new DetectorException(message, e);
        }

        try {
            List<DetectorMapping> result = objectMapper.readValue(
                    content.asBytes(),
                    new TypeReference<List<DetectorMapping>>() {});
            if (result == null) {
                throw new IOException("Updated detector mappings are null");
            }
            return result;
        } catch (IOException e) {
            val message = "IOException while reading updated detectors mappings: timeInSecs=" + timeInSecs;
            throw new DetectorException(message, e);
        }
    }
}
