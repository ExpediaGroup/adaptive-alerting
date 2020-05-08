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

import com.expedia.adaptivealerting.anomdetect.source.DetectorDocument;
import com.expedia.adaptivealerting.modelservice.exception.RecordNotFoundException;
import com.expedia.adaptivealerting.modelservice.repo.LegacyDetectorRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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

@Deprecated
@RestController
@RequestMapping(path = "/api/v2/detectors")
@Slf4j
public class LegacyDetectorController {

    @Autowired
    private LegacyDetectorRepository detectorRepo;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String createDetector(@Valid @RequestBody DetectorDocument document) {
        val uuid = detectorRepo.createDetector(document);
        return uuid.toString();
    }

    @GetMapping(path = "/findByUuid", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public DetectorDocument findByUuid(@RequestParam String uuid) {
        DetectorDocument detector = detectorRepo.findByUuid(uuid);
        if (detector == null) {
            throw new RecordNotFoundException("Invalid UUID: " + uuid);
        }
        return detector;
    }

    @GetMapping(path = "/findByCreatedBy", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public List<DetectorDocument> findByCreatedBy(@RequestParam String user) {
        List<DetectorDocument> detectors = detectorRepo.findByCreatedBy(user);
        if (detectors == null || detectors.isEmpty()) {
            // TODO: This should be RecordNotFoundException
            throw new IllegalArgumentException("Invalid user: " + user);
        }
        return detectors;
    }

    @PostMapping(path = "/toggleDetector", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public void toggleDetector(@RequestParam String uuid, @RequestParam Boolean enabled) {
        Assert.notNull(uuid, "uuid can't be null");
        Assert.notNull(enabled, "enabled can't be null");
        detectorRepo.toggleDetector(uuid, enabled);
    }

    @PostMapping(path = "/trustDetector", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public void trustDetector(@RequestParam String uuid, @RequestParam Boolean trusted) {
        Assert.notNull(uuid, "uuid can't be null");
        Assert.notNull(trusted, "trusted can't be null");
        detectorRepo.trustDetector(uuid, trusted);
    }

    @GetMapping(path = "/getLastUpdatedDetectors", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public List<DetectorDocument> getLastUpdatedOrUsedDetectors(@RequestParam long interval) {
        return detectorRepo.getLastUpdatedDetectors(interval);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public void updateDetector(@RequestParam String uuid, @RequestBody DetectorDocument document) {
        detectorRepo.updateDetector(uuid, document);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public void deleteDetector(@RequestParam String uuid) {
        detectorRepo.deleteDetector(uuid);
    }
}
