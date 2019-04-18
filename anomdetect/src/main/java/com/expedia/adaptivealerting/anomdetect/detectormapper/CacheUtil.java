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
package com.expedia.adaptivealerting.anomdetect.detectormapper;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
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

    private static final String DELIMITER = "->";

    public static String getKey(Map<String, String> tags) {
        List<String> listOfEntries = tags.entrySet()
                .stream()
                .map(entry -> entry.getKey() + DELIMITER + entry.getValue())
                .sorted()
                .collect(Collectors.toList());
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

    public static String getDetectorIds(List<Detector> detectors) {
        List<String> result = new ArrayList<>();
        detectors.forEach(detector -> {
            result.add(detector.getUuid().toString());
        });
        return String.join("|", result);
    }

    public static List<Detector> buildDetectors(String bunchOfDetectorIds) {
        if (bunchOfDetectorIds == null || "".equals(bunchOfDetectorIds)) {
            return Collections.emptyList();
        }
        String[] detectorList = bunchOfDetectorIds.split("\\|");
        return Arrays.asList(detectorList).stream()
                .map(detector -> new Detector(UUID.fromString(detector)))
                .collect(Collectors.toList());
    }
}
