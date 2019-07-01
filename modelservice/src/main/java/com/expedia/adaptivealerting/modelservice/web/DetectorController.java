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

import com.expedia.adaptivealerting.modelservice.entity.Detector;
import com.expedia.adaptivealerting.modelservice.service.DetectorService;
import com.expedia.adaptivealerting.modelservice.util.RequestValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/api/v2/detectors")
public class DetectorController {

    @Autowired
    private DetectorService detectorService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String createDetector(@Valid @RequestBody Detector detector) {
        RequestValidator.validateDetector(detector);
        return detectorService.createDetector(detector);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public void updateDetector(@RequestParam String uuid, @RequestBody Detector detector) {
        RequestValidator.validateDetector(detector);
        detectorService.updateDetector(uuid, detector);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public void deleteDetector(@RequestParam String uuid) {
        detectorService.deleteDetector(uuid);
    }

    @GetMapping(path = "/findByUuid", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public Detector findByUuid(@RequestParam String uuid) {
        return detectorService.findByUuid(uuid);
    }

    @GetMapping(path = "/findByCreatedBy", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public List<Detector> findByCreatedBy(@RequestParam String user) {
        return detectorService.findByCreatedBy(user);
    }

    @PostMapping(path = "/toggleDetector", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public void toggleDetector(@RequestParam String uuid, @RequestParam Boolean enabled) {
        Assert.notNull(uuid, "uuid can't be null");
        Assert.notNull(enabled, "enabled can't be null");
        detectorService.toggleDetector(uuid, enabled);
    }

    @GetMapping(path = "/getLastUpdatedDetectors", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public List<Detector> getLastUpdatedDetectors(@RequestParam long interval) {
        return detectorService.getLastUpdatedDetectors(interval);
    }

} 