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
package com.expedia.adaptivealerting.modelservice.repo;


import com.expedia.adaptivealerting.modelservice.entity.ElasticsearchDetector;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data repository for detectors stored in elastic search.
 */
public interface ElasticsearchDetectorRepository extends ElasticsearchRepository<ElasticsearchDetector, String>, ElasticsearchDetectorRepoCustom {

    /**
     * Finds a detector by its unique uuid, if any.
     *
     * @param uuid Detector uuid.
     * @return Detector identified by the unique key.
     */
    ElasticsearchDetector findElasticSearchDetectorByUuid(@Param("uuid") String uuid);

    /**
     * Finds a list of detectors created by provided user, if any.
     *
     * @param user Detector user.
     * @return List of detectors for the provided user.
     */
    List<ElasticsearchDetector> findElasticSearchDetectorByCreatedBy(@Param("user") String user);

}

