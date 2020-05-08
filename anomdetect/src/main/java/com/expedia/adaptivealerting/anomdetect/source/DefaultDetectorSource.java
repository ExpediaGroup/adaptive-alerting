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

import com.expedia.adaptivealerting.anomdetect.detect.Detector;
import com.expedia.adaptivealerting.anomdetect.mapper.DetectorMapping;
import com.expedia.adaptivealerting.anomdetect.mapper.DetectorMatchResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isTrue;
import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

// Non-final because we currently need to mock this in DetectorManagerTest. [WLW]

/**
 * The default {@link DetectorSource}, backed by the Model Service.
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultDetectorSource implements DetectorSource {

    /**
     * Client to load detector documents from the Model Service.
     */
    @NonNull
    private final DetectorClient client;

    @NonNull
    private final DetectorFactory registry;

    @Override
    public DetectorMatchResponse findDetectorMappings(List<Map<String, String>> metricTags) {
        notNull(metricTags, "metricTags can't be null");
        return client.findMatchingDetectorMappings(metricTags);
    }


    @Override
    public List<DetectorMapping> findDetectorMappingsUpdatedSince(long lastModifiedTime) {
        isTrue(lastModifiedTime >= 0, "Required: lastModifiedTime >= 0");
        return client.findDetectorMappingsUpdatedSince(lastModifiedTime);
    }


    @Override
    public List<DetectorMapping> findUpdatedDetectorMappings(long timePeriod) {
        isTrue(timePeriod > 0, "Required: timePeriod > 0");
        return client.findUpdatedDetectorMappings(timePeriod);
    }

    @Override
    public DetectorMapping findDetectorMappingByUuid(UUID uuid) {
        notNull(uuid, "uuid can't be null");
        return client.findDetectorMappingByUuid(uuid);
    }


    @Override
    public Detector findDetector(UUID uuid) {
        notNull(uuid, "uuid can't be null");
        val document = client.findDetectorDocument(uuid);
        return registry.buildDetector(document);
    }

    @Override
    public List<UUID> findUpdatedDetectors(long timePeriod) {
        isTrue(timePeriod > 0, "Required: timePeriod > 0");
        return client
                .findUpdatedDetectorDocuments(timePeriod)
                .stream()
                .map(document -> document.getUuid())
                .collect(Collectors.toList());
    }

    @Override
    public long getEnabledDetectorMappingCount(){
        return client
                .getEnabledDetectorMappingCount();
    }
}
