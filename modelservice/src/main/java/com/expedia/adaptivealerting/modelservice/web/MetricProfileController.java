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

import com.expedia.adaptivealerting.modelservice.dto.metricprofiling.CreateMetricProfilingRequest;
import com.expedia.adaptivealerting.modelservice.service.MetricProfilingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/metricProfiling/search")
public class MetricProfileController {

    @Autowired
    private MetricProfilingService metricProfilingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String createMetricProfile(@RequestBody CreateMetricProfilingRequest request) {
        request.validate();
        return metricProfilingService.createMetricProfile(request);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public void updateMetricProfile(@RequestParam String id, @RequestParam Boolean isStationary) {
        metricProfilingService.updateMetricProfile(id, isStationary);
    }

    @PostMapping
    @RequestMapping(value = "/findByTags")
    public Boolean profilingExists(@RequestBody Map<String, String> tags) {
        return metricProfilingService.profilingExists(tags);
    }

}
