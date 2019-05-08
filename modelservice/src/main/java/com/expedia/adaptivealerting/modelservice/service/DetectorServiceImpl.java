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

import com.expedia.adaptivealerting.modelservice.entity.ElasticSearchDetector;
import com.expedia.adaptivealerting.modelservice.repo.ElasticSearchDetectorRepository;
import com.expedia.adaptivealerting.modelservice.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service to fetch and modify detectors stored in elastic search
 */
@Slf4j
@Service
public class DetectorServiceImpl implements DetectorService {

    @Autowired
    private ElasticSearchDetectorRepository elasticSearchDetectorRepository;

    public void toggleDetector(String uuid, Boolean enabled) {
        ElasticSearchDetector detector = elasticSearchDetectorRepository.findElasticSearchDetectorByUuid(uuid);
        elasticSearchDetectorRepository.toggleDetector(detector, enabled);
    }

    public List<ElasticSearchDetector> getLastUpdatedDetectors(int interval) {
        Instant now = DateUtil.now().toInstant();
        String toDate = DateUtil.toUtcDateString(now);
        String fromDate = DateUtil.toUtcDateString((now.minus(interval, ChronoUnit.MINUTES)));
        return elasticSearchDetectorRepository.getLastUpdatedDetectors(fromDate, toDate);
    }
}
