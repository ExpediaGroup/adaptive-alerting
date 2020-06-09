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
package com.expedia.adaptivealerting.modelservice.web;

import com.expedia.adaptivealerting.anomdetect.util.AssertUtil;
import com.expedia.adaptivealerting.modelservice.domain.mapping.DetectorMapping;
import com.expedia.adaptivealerting.modelservice.exception.RecordNotFoundException;
import com.expedia.adaptivealerting.modelservice.repo.DetectorMappingRepository;
import com.expedia.adaptivealerting.modelservice.web.request.CreateDetectorMappingRequest;
import com.expedia.adaptivealerting.modelservice.web.request.SearchMappingsRequest;
import com.expedia.adaptivealerting.modelservice.web.response.MatchingDetectorsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/detectorMappings")
public class DetectorMappingController {

    @Autowired
    private DetectorMappingRepository detectorMappingRepo;

    @RequestMapping(produces = "application/json", method = RequestMethod.GET)
    public DetectorMapping getDetectorMapping(@RequestParam String id) {
        DetectorMapping detectorMapping = detectorMappingRepo.findDetectorMapping(id);
        if (detectorMapping == null) {
            throw new RecordNotFoundException("Invalid mapping id: " + id);
        }
        return detectorMapping;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public String createDetectorMapping(@RequestBody CreateDetectorMappingRequest request) {
        request.validate();
        return detectorMappingRepo.createDetectorMapping(request);
    }

    @RequestMapping(value = "/disable", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void disableDeleteDetectorMapping(@RequestParam String id) {
        AssertUtil.notNull(id, "id can't be null");
        detectorMappingRepo.disableDetectorMapping(id);
    }

    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteDetectorMapping(@RequestParam String id) {
        AssertUtil.notNull(id, "id can't be null");
        detectorMappingRepo.deleteDetectorMapping(id);
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public List<DetectorMapping> searchDetectorMapping(@RequestBody SearchMappingsRequest request) {
        AssertUtil.isTrue(request.getUserId() != null || request.getDetectorUuid() != null,
                "User id and Detector UUID can't both be null");
        return detectorMappingRepo.search(request);
    }

    @RequestMapping(value = "/lastUpdated", method = RequestMethod.GET)
    public List<DetectorMapping> findDetectorMapping(@RequestParam int timeInSecs) {
        AssertUtil.notNull(timeInSecs, "timeInSecs can't be null");
        return detectorMappingRepo.findLastUpdated(timeInSecs);
    }

    @RequestMapping(value = "/findMatchingByTags", method = RequestMethod.POST)
    public MatchingDetectorsResponse searchDetectorMapping(@RequestBody List<Map<String, String>> tagsList) {
        MatchingDetectorsResponse matchingDetectorMappings = detectorMappingRepo.findMatchingDetectorMappings(tagsList);
        return matchingDetectorMappings;
    }

    @RequestMapping(value = "/deleteByDetectorUuid", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteMappingsByDetectorUUID(@RequestParam UUID uuid) {
        AssertUtil.notNull(uuid, "detector uuid can't be null");
        detectorMappingRepo.deleteMappingsByDetectorUUID(uuid);
    }
}
