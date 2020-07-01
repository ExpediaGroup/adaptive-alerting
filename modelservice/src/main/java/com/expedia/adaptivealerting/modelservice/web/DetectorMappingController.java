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
import com.expedia.www.haystack.client.Span;
import io.opentracing.SpanContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import com.expedia.adaptivealerting.modelservice.tracing.Trace;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/detectorMappings")
public class DetectorMappingController {

    @Autowired
    private DetectorMappingRepository detectorMappingRepo;

    @Autowired
    private Trace trace;

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
    public String createDetectorMapping(@RequestBody CreateDetectorMappingRequest request, @RequestHeader HttpHeaders header) {
        SpanContext parentSpanContext = trace.extractParentSpan(header);
        Span span = trace.startSpan("create-mappings", parentSpanContext);
        request.validate();
        String detectorMappingJsonString = detectorMappingRepo.createDetectorMapping(request);
        span.finish();
        return detectorMappingJsonString;
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
    public List<DetectorMapping> searchDetectorMapping(@RequestBody SearchMappingsRequest request,
                                                       @RequestHeader HttpHeaders httpHeaders) {
        SpanContext parentSpanContext = trace.extractParentSpan(httpHeaders);
        Span span = trace.startSpan("mappings-search", parentSpanContext);
        AssertUtil.isTrue(request.getUserId() != null || request.getDetectorUuid() != null,
                "User id and Detector UUID can't both be null");
        List<DetectorMapping> resultSearchDetectorMapping = detectorMappingRepo.search(request);
        span.finish();
        return resultSearchDetectorMapping;
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

    @RequestMapping(value = "/deleteMappingsByDetectorUuid", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteMappingsByDetectorUUID(@RequestParam UUID uuid) {
        AssertUtil.notNull(uuid, "detector uuid can't be null");
        detectorMappingRepo.deleteMappingsByDetectorUUID(uuid);
    }
}
