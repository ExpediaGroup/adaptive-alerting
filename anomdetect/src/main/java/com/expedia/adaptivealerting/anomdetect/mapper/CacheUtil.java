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

import java.util.*;
import java.util.stream.Collectors;

/**
 * CacheUtil utilities.
 */
@Slf4j
public final class CacheUtil {

    private static final String DELIMITER = "=";

    public static String getKey(Map<String, String> tags) {
        List<String> listOfEntries = tags.entrySet()
                .stream()
                .map(entry -> entry.getKey() + DELIMITER + entry.getValue()).sorted().collect(Collectors.toList());
        return String.join(",", listOfEntries);
    }

    public static Map<String, String> getTags(String hashKey) {
        String[] keyVals = hashKey.split(",");
        Map<String, String> tags = new HashMap<>();
        Arrays.asList(keyVals).forEach(keyVal -> {
            String[] kv = keyVal.split(DELIMITER);
            tags.put(kv[0], kv[1]);
        });
        return tags;
    }

    public static String getDetectorIdsString(List<Detector> list) {
        List<String> result = new ArrayList<>();
        list.forEach(detector -> {
            result.add(detector.getUuid().toString());
        });
        return String.join("|", result);
    }

    public static List<Detector> buildDetectors(String detectorIdsString) {
        if (detectorIdsString == null || "".equals(detectorIdsString)) {
            return Collections.emptyList();
        }
        String[] detectorList = detectorIdsString.split("\\|");
        return Arrays.asList(detectorList).stream().map(dt -> new Detector(UUID.fromString(dt))).collect(Collectors.toList());
    }
}
