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
package com.expedia.adaptivealerting.modelservice.repo;

import com.expedia.adaptivealerting.modelservice.entity.DetectorMapping;
import com.expedia.adaptivealerting.modelservice.repo.request.CreateDetectorMappingRequest;
import com.expedia.adaptivealerting.modelservice.repo.request.SearchMappingsRequest;
import com.expedia.adaptivealerting.modelservice.repo.response.MatchingDetectorsResponse;

import java.util.List;
import java.util.Map;

public interface DetectorMappingRepository {

    /**
     * Creates a new detector mapping in the persistent store. This maps a set of metrics to a detector.
     *
     * @param request Mapping request
     * @return Mapping ID
     */
    String createDetectorMapping(CreateDetectorMappingRequest request);

    MatchingDetectorsResponse findMatchingDetectorMappings(List<Map<String, String>> tagsList);

    DetectorMapping findDetectorMapping(String id);

    List<DetectorMapping> findLastUpdated(int timeInSeconds);

    List<DetectorMapping> search(SearchMappingsRequest request);

    void disableDetectorMapping(String id);

    void deleteDetectorMapping(String id);
}
