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
package com.expedia.adaptivealerting.modelservice.service;

import com.expedia.adaptivealerting.modelservice.dto.detectormapping.CreateDetectorMappingRequest;
import com.expedia.adaptivealerting.modelservice.entity.ElasticsearchDetectorMapping;
import com.expedia.adaptivealerting.modelservice.dto.detectormapping.MatchingDetectorsResponse;
import com.expedia.adaptivealerting.modelservice.dto.detectormapping.SearchMappingsRequest;
import com.expedia.adaptivealerting.modelservice.repo.EsDetectorMappingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service to fetch and modify metric detector mappings in elastic search
 */
@Service
@Slf4j
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.AvoidThrowingRawExceptionTypes"})
public class DetectorMappingServiceImpl implements DetectorMappingService {

    @Autowired
    private EsDetectorMappingRepository detectorMappingRepository;

    @Override
    public MatchingDetectorsResponse findMatchingDetectorMappings(List<Map<String, String>> tagsList) {
        return detectorMappingRepository.findMatchingDetectorMappings(tagsList);
    }

    @Override
    public String createDetectorMapping(CreateDetectorMappingRequest createRequest) {
        return detectorMappingRepository.createDetectorMapping(createRequest);
    }

    @Override
    public void deleteDetectorMapping(String id) {
        detectorMappingRepository.deleteDetectorMapping(id);
    }

    @Override
    public void disableDetectorMapping(String id) {
        detectorMappingRepository.disableDetectorMapping(id);
    }

    @Override
    public ElasticsearchDetectorMapping findDetectorMapping(String id) {
        return detectorMappingRepository.findDetectorMapping(id);
    }

    @Override
    public List<ElasticsearchDetectorMapping> search(SearchMappingsRequest searchMappingsRequest) {
        return detectorMappingRepository.search(searchMappingsRequest);
    }

    @Override
    public List<ElasticsearchDetectorMapping> findLastUpdated(int timeInSeconds) {
        return detectorMappingRepository.findLastUpdated(timeInSeconds);
    }
}
