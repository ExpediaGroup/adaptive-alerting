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
import com.expedia.adaptivealerting.modelservice.repo.TagRepository;
import com.expedia.adaptivealerting.modelservice.service.OnboardService;
import com.expedia.adaptivealerting.modelservice.util.JpaConverterJson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for OnboardService.
 *
 * @author tbahl
 */
@Slf4j
@RestController
public class OnboardController {

    @Autowired
    private OnboardService onboardService;

    @PostMapping(path = "/onboarded")
    public Long onboarded(@RequestBody Metric metric) {
        return onboardService.onboarded(metric);
    }
}

