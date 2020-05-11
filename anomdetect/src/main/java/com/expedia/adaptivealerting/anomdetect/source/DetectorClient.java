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
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.client.fluent.Content;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isTrue;
import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

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
@RequiredArgsConstructor
@Slf4j
public class DetectorClient {
    static final String FIND_DOCUMENT_PATH = "/api/v2/detectors/findByUuid?uuid=%s";
    static final String FIND_UPDATED_DOCUMENTS_PATH = "/api/v2/detectors/getLastUpdatedDetectors?interval=%d";

    // TODO Shouldn't these also include the /api/v2 prefix? [WLW]
    static final String FIND_MAPPINGS_BY_TAGS_PATH = "/api/detectorMappings/findMatchingByTags";
    static final String FIND_UPDATED_MAPPINGS_PATH = "/api/detectorMappings/lastUpdated?timeInSecs=%d";
    static final String FIND_UPDATED_SINCE_MAPPINGS_PATH = "/api/detectorMappings/updatedSince?lastModifiedTime=%d";
    static final String FIND_MAPPINGS_BY_UUID_PATH = "/api/detectorMappings/search";
    static final String GET_ENABLED_MAPPINGS_PATH = "/api/detectorMappings/count";


    @NonNull
    private final HttpClientWrapper httpClient;

    @NonNull
    private final String baseUri;

    @NonNull
    private final ObjectMapper objectMapper;

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
        val uri = String.format(baseUri + FIND_DOCUMENT_PATH, uuid);

        Content content;
        try {
            content = httpClient.get(uri);
        } catch (IOException e) {
            val message = "IOException while getting detector document " + uuid +
                    ": httpMethod=GET" +
                    ", uri=" + uri +
                    ", message=" + e.getMessage();
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
     * @param timeInSecs the time period in seconds
     * @return the list of detectorMappings that were modified in last since minutes
     */
    public List<DetectorDocument> findUpdatedDetectorDocuments(long timeInSecs) {
        isTrue(timeInSecs > 0, "sinceSeconds must be strictly positive");

        val uri = String.format(baseUri + FIND_UPDATED_DOCUMENTS_PATH, timeInSecs);
        Content content;
        try {
            content = httpClient.get(uri);
        } catch (IOException e) {
            val message = "IOException while getting last updated detectors" +
                    ": sinceSeconds=" + timeInSecs +
                    ", httpMethod=GET" +
                    ", uri=" + uri;
            throw new DetectorException(message, e);
        }

        try {
            return Arrays.asList(objectMapper.readValue(content.asBytes(), DetectorDocument[].class));
        } catch (IOException e) {
            val message = "IOException while reading detectors: sinceSeconds=" + timeInSecs;
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

        val uri = baseUri + FIND_MAPPINGS_BY_TAGS_PATH;
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
     * Find detector mappings updated since timestamp.
     *
     * @param lastModifiedTime the last modified timestamp in milliseconds
     * @return the list of detectormappings that were modified in last since minutes
     */
    public List<DetectorMapping> findDetectorMappingsUpdatedSince(long lastModifiedTime) {
        Content content;
        List<DetectorMapping> result;

        val uri = String.format(baseUri + FIND_UPDATED_SINCE_MAPPINGS_PATH, lastModifiedTime);
        try {
            content = httpClient.get(uri);
        } catch (IOException e) {
            val message = "IOException while getting updated detectors mappings" +
                    ": lastModifiedTime=" + lastModifiedTime +
                    ", httpMethod=GET" +
                    ", uri=" + uri;
            throw new DetectorException(message, e);
        }

        val typeRef = new TypeReference<List<DetectorMapping>>() {
        };
        try {
            result = objectMapper.readValue(content.asBytes(), typeRef);
        } catch (IOException e) {
            val message = "IOException while reading updated detectors mappings" +
                    ": lastModifiedTime=" + lastModifiedTime;
            throw new DetectorException(message, e);
        }

        if (result == null) {
            throw new DetectorException("Updated since detector mappings are null");
        }

        return result;
    }

    /**
     * Find updated detector mappings list.
     *
     * @param timeInSecs the time period in seconds
     * @return the list of detectormappings that were modified in last since minutes
     */
    public List<DetectorMapping> findUpdatedDetectorMappings(long timeInSecs) {
        Content content;
        List<DetectorMapping> result;

        val uri = String.format(baseUri + FIND_UPDATED_MAPPINGS_PATH, timeInSecs);
        try {
            content = httpClient.get(uri);
        } catch (IOException e) {
            val message = "IOException while getting updated detectors mappings" +
                    ": timeInSecs=" + timeInSecs +
                    ", httpMethod=GET" +
                    ", uri=" + uri;
            throw new DetectorException(message, e);
        }

        val typeRef = new TypeReference<List<DetectorMapping>>() {
        };
        try {
            result = objectMapper.readValue(content.asBytes(), typeRef);
        } catch (IOException e) {
            val message = "IOException while reading updated detectors mappings" +
                    ": timeInSecs=" + timeInSecs;
            throw new DetectorException(message, e);
        }

        if (result == null) {
            throw new DetectorException("Updated detector mappings are null");
        }

        return result;
    }

    /**
     * Finds a detector mapping for the given detector UUID
     *
     * @param uuid Detector UUID.
     * @return Detector mapping
     */
    public DetectorMapping findDetectorMappingByUuid(UUID uuid) {
        Content content;
        List<DetectorMapping> result;

        val uri = String.format(baseUri + FIND_MAPPINGS_BY_UUID_PATH);
        val bodyMap = Collections.singletonMap("detectorUuid", uuid);
        try {
            val body = objectMapper.writeValueAsString(bodyMap);
            content = httpClient.post(uri, body);
        } catch (IOException e) {
            val message = "IOException while getting detectors mappings for" +
                    ": uuid=" + uuid +
                    ", httpMethod=POST" +
                    ", uri=" + uri;
            throw new DetectorException(message, e);
        }
        val typeRef = new TypeReference<List<DetectorMapping>>() {
        };
        try {
            result = objectMapper.readValue(content.asBytes(), typeRef);
        } catch (IOException e) {
            val message = "IOException while reading detectors mappings for" +
                    ": uuid=" + uuid;
            throw new DetectorException(message, e);
        }

        if (result == null || result.isEmpty()) {
            throw new DetectorException("No detector mappings for" +
                    ": uuid=" + uuid);
        }
        return result.get(0);
    }

 /**
     * Gets the total count of detector mappings
     *
     * @return Detector mapping count
     */
    public long getEnabledDetectorMappingCount () {
        Content content;
        long result = 0;

        val uri = String.format(baseUri + GET_ENABLED_MAPPINGS_PATH);
        try {
            content = httpClient.get(uri);
        } catch (IOException e) {
            val message = "IOException while getting detector mapping count, uri=" + uri;
            throw new DetectorException(message, e);
        }
        val typeRef = new TypeReference<Long>() {
        };
        try {
            result = objectMapper.readValue(content.asBytes(), typeRef);
        } catch (IOException e) {
            val message = "IOException while reading detector mappings count";
            throw new DetectorException(message, e);
        }
        return result;
    }

}
