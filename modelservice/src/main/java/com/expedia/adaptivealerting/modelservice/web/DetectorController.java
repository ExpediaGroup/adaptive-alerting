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

import com.expedia.adaptivealerting.modelservice.entity.ElasticsearchDetector;
import com.expedia.adaptivealerting.modelservice.service.DetectorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/api/elasticSearchDetectors")
@Slf4j
public class DetectorController {

    @Autowired
    private DetectorService detectorService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String createDetector(@RequestBody ElasticsearchDetector elasticsearchDetector) {
        return detectorService.createDetector(elasticsearchDetector);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public void updateDetector(@RequestParam String uuid, @RequestBody ElasticsearchDetector elasticsearchDetector) {
        detectorService.updateDetector(uuid, elasticsearchDetector);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public void deleteDetector(@RequestParam String uuid) {
        detectorService.deleteDetector(uuid);
    }

    @GetMapping(path = "/findByUuid", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public List<ElasticsearchDetector> findByUuid(@RequestParam String uuid) {
        return detectorService.findByUuid(uuid);
    }

    @GetMapping(path = "/findByCreatedBy", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public List<ElasticsearchDetector> findByCreatedBy(@RequestParam String user) {
        return detectorService.findByCreatedBy(user);
    }

    @PostMapping(path = "/toggleDetector", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public void toggleDetector(@RequestParam String uuid, @RequestParam Boolean enabled) {
        detectorService.toggleDetector(uuid, enabled);
    }

    @GetMapping(path = "/getLastUpdatedDetectors", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public List<ElasticsearchDetector> getLastUpdatedDetectors(@RequestParam int interval) {
        return detectorService.getLastUpdatedDetectors(interval);
    }

} 