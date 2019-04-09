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

import com.expedia.adaptivealerting.modelservice.dao.*;
import com.expedia.adaptivealerting.modelservice.model.CreateDetectorMappingRequest;
import com.expedia.adaptivealerting.modelservice.model.DetectorMapping;
import com.expedia.adaptivealerting.modelservice.model.MatchingDetectorsResponse;
import com.expedia.adaptivealerting.modelservice.model.SearchMappingsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DetectorMappingController {

    @Autowired
    private DetectorMappingService detectorMappingService;
    
    @Autowired
    private RequestValidator requestValidator;

    @RequestMapping(value = "/detector-mapping", produces = "application/json", method = RequestMethod.GET)
    public DetectorMapping getDetectorMapping(@RequestParam String id) {
        return detectorMappingService.findDetectorMapping(id);
    }

    @RequestMapping(value = "/detector-mapping", method = RequestMethod.POST)
    public ResponseEntity<String> createDetectorMapping(@RequestBody CreateDetectorMappingRequest request) {
        requestValidator.validateExpression(request.getExpression());
        requestValidator.validateUser(request.getUser());
        requestValidator.validateDetector(request.getDetector());
        return new ResponseEntity(detectorMappingService.createDetectorMapping(request), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/detector-mapping/disable", method = RequestMethod.PUT)
    public ResponseEntity disableDeleteDetectorMapping(@RequestParam String id) {
        detectorMappingService.disableDetectorMapping(id);
        return new ResponseEntity(HttpStatus.OK);
    }
    
    @RequestMapping(value = "/detector-mapping", method = RequestMethod.DELETE)
    public ResponseEntity deleteDetectorMapping(@RequestParam String id) {
        detectorMappingService.deleteDetectorMapping(id);
        return new ResponseEntity(HttpStatus.OK);
    }
    
    @RequestMapping(value = "/detector-mapping/search", method = RequestMethod.POST)
    public List<DetectorMapping> searchDetectorMapping(@RequestBody SearchMappingsRequest request) {
        Assert.isTrue(request.getUserId() != null || request.getDetectorUuid() != null,
                "Both userId and detectorId can't be null");
        return detectorMappingService.search(request);
    }
    
    @RequestMapping(value = "/detector-mapping/last-updated", method = RequestMethod.GET)
    public List<DetectorMapping> findDetectorMapping(@RequestParam int timeInSecs) {
        Assert.notNull(timeInSecs, "timeInSecs can't be null");
        return detectorMappingService.findLastUpdated(timeInSecs);
    }

    @RequestMapping(value = "/detector-mapping/findMatchingByTags", method = RequestMethod.POST)
    public MatchingDetectorsResponse searchDetectorMapping(@RequestBody List<Map<String, String>> tagsList) {
        return detectorMappingService.findMatchingDetectorMappings(tagsList);
    }
}
