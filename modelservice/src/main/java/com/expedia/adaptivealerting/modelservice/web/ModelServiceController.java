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

import com.expedia.adaptivealerting.modelservice.entity.Metric;
import com.expedia.adaptivealerting.modelservice.entity.Tag;
import com.expedia.adaptivealerting.modelservice.entity.UserInfo;
import com.expedia.adaptivealerting.modelservice.service.ModelService;
import com.expedia.adaptivealerting.modelservice.service.SignUpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class ModelServiceController {

    @Autowired
    private ModelService modelService;

    @Autowired
    private ModelService metricFinder;

    @Autowired
    private SignUpService signUpService;

    /**
     * This controller is for checking whether metric is onboarded
     * or it has to be onboarded.
     * @param metric
     * @return
     */
    @PostMapping(path = "/onboard")
    private Metric onboard(@RequestBody Metric metric) {
        return modelService.onboard(metric);
    }

    /**
     * New user sign up
     *
     * @param user
     * @return
     */
    @PostMapping(path = "/signUp")
    public ResponseEntity<?> addUser(@ModelAttribute UserInfo user) {
        UserInfo newUser = signUpService.addUser(user);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * Metric Search with list of tags.
     *
     * @param tagList
     * @return
     */
    @PostMapping(path="/findMetricsByTags")
    private List metricfinder(@RequestBody List<Tag> tagList){return metricFinder.metricfinder(tagList);}
}

