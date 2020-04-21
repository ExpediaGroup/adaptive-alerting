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
package com.expedia.adaptivealerting.anomdetect.mapper;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * CacheUtil utilities.
 */
@Slf4j
public final class CacheUtil {

    private static final String CACHE_KEY_DELIMITER = "->";
    private static final String CACHE_VALUE_DELIMITER = "|";

    /**
     * Converts map to cache's key format
     *
     * @param tags A Map {k1=v1, k2=v2, k3=v3}
     * @return Returns a string in this format "k1->v1,k2->v2,k3->v3"
     */
    public static String getKey(Map<String, String> tags) {
        List<String> listOfEntries = tags.entrySet()
                .stream()
                .map(entry -> {
                    String encodedValue = Base64.getEncoder().encodeToString(entry.getValue().getBytes());
                    return entry.getKey() + CACHE_KEY_DELIMITER + encodedValue;
                })
                .sorted()
                .collect(Collectors.toList());
        return String.join(",", listOfEntries);
    }

    /**
     * Converts cache key in string format to map
     *
     * @param key String "k1->v1,k2->v2,k3->v3"
     * @return Returns a map in this format {k1=v1, k2=v2, k3=v3}
     */
    public static Map<String, String> getTags(String key) {
        String[] keyVals = key.split(",");
        Map<String, String> tags = new HashMap<>();
        Arrays.asList(keyVals).forEach(keyVal -> {
            String[] kv = keyVal.split(CACHE_KEY_DELIMITER);
            byte[] decodedValue = Base64.getDecoder().decode(kv[1]);
            String value = new String(decodedValue);
            tags.put(kv[0], value);
        });
        return tags;
    }

    /**
     * Converts list of detectors to cache's value format
     *
     * @param detectors A list of detectors [Detector(consumerId=c1, uuid=uuid1), Detector(consumerId=c2, uuid=uuid)]
     * @return Returns a string in this format "c1,uuid1|c2,uuid2"
     */
    public static String getValue(List<Detector> detectors) {
        List<String> result = new ArrayList<>();
        detectors.forEach(detector -> {
            result.add(detector.getConsumerId() + "," + detector.getUuid().toString());
        });
        return String.join(CACHE_VALUE_DELIMITER, result);
    }

    /**
     * Converts cache's value format to list of detectors
     *
     * @param detectorInfo A String "c1,uuid1|c2,uuid2"
     * @return Returns a list of detectors in this format [Detector(consumerId=c1, uuid=uuid1), Detector(consumerId=c2, uuid=uuid)]
     */
    public static List<Detector> buildDetectors(String detectorInfo) {
        if (detectorInfo == null || "".equals(detectorInfo)) {
            return Collections.emptyList();
        }
        String[] detectorList = detectorInfo.split("\\|");
        return Arrays.asList(detectorList).stream()
                .map(detector -> new Detector(detector.split(",")[0], UUID.fromString(detector.split(",")[1]))) //update
                .collect(Collectors.toList());
    }
}
