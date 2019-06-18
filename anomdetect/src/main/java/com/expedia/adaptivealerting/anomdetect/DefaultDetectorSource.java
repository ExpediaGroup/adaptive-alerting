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
package com.expedia.adaptivealerting.anomdetect;

import com.expedia.adaptivealerting.anomdetect.connector.ModelServiceConnector;
import com.expedia.adaptivealerting.anomdetect.detectormapper.DetectorMapping;
import com.expedia.adaptivealerting.anomdetect.detectormapper.DetectorMatchResponse;
import com.expedia.adaptivealerting.anomdetect.outlier.legacy.LegacyDetectorFactory;
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
 * A {@link DetectorSource} backed by the Model Service.
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultDetectorSource implements DetectorSource {

    @NonNull
    private final ModelServiceConnector connector;

    @NonNull
    private final LegacyDetectorFactory legacyDetectorFactory;

    @Override
    public Detector findDetector(UUID uuid) {
        notNull(uuid, "uuid can't be null");
        val detectorResource = connector.findLatestDetector(uuid);
        return legacyDetectorFactory.createDetector(uuid, detectorResource);
    }

    @Override
    public List<UUID> findUpdatedDetectors(int timePeriod) {
        notNull(timePeriod, "timePeriod can't be null");
        return connector
                .findUpdatedDetectors(timePeriod)
                .stream()
                .map(resource -> UUID.fromString(resource.getUuid()))
                .collect(Collectors.toList());
    }

    @Override
    public List<DetectorMapping> findUpdatedDetectorMappings(int timeInSecs) {
        isTrue(timeInSecs > 0, "timeInSecs must be strictly positive");

        return connector
                .findUpdatedDetectorMappings(timeInSecs);
    }


    @Override
    public DetectorMatchResponse findMatchingDetectorMappings(List<Map<String, String>> metricTags) {
        return connector.findMatchingDetectorMappings(metricTags);
    }
}
