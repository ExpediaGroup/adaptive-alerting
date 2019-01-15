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

import com.expedia.adaptivealerting.modelservice.entity.Metric;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data repository for metrics.
 *
 * @author kashah
 * @author Willie Wheeler
 */
public interface MetricRepository extends PagingAndSortingRepository<Metric, Long> {

    /**
     * Finds a metric by its unique key, if any.
     *
     * @param key Unique key.
     * @return Metric identified by the unique key.
     */
    Metric findByKey(@Param("key") String key);

    /**
     * Finds a metric by its unique hash.
     *
     * @param hash Unique hash.
     * @return Metric identified by the unique hash.
     */
    Metric findByHash(@Param("hash") String hash);


    /**
     * Finds a list of metrics by its user.
     *
     * @param user Detector user.
     * @return List of metrics by its user
     */
    @Query(nativeQuery = true, value = "SELECT * FROM metric WHERE id IN (SELECT metric_id FROM metric_detector_mapping WHERE detector_id IN (SELECT id FROM detector WHERE created_by=:user))")
    List<Metric> findByCreatedBy(@Param("user") String user);

    /**
     * Finds a list of metrics by its matching key.
     *
     * @param key Matching key.
     * @return List of metrics by its matching key
     */
    @Query(nativeQuery = true, value = "SELECT * FROM metric m WHERE m.ukey LIKE :key order by m.ukey", countQuery = "SELECT count(*) FROM metric m WHERE m.ukey LIKE :key")
    Page<Metric> findByKeyContaining(@Param("key") String key, Pageable pageable);

    /**
     * Finds a list of metrics by its matching tag key and value.
     * @param tagKey
     * @param tagValue
     * @param pageable
     * @return List of metrics containing tagKey and tagValue.
     */
    @Query(nativeQuery = true, value = "SELECT m.* FROM metric m INNER JOIN metric_tag_mapping mm on mm.metric_id = m.id  INNER JOIN tag t on t.id = mm.tag_id WHERE t.ukey = :tagKey AND t.uvalue = :tagValue")
    List<Metric> findByTagContaining(@Param("tagKey") String tagKey, @Param("tagValue") String tagValue, Pageable pageable);

    /**
     * Finds a list of metrics attached to a given detector
     *
     * @param uuid Detector uuid.
     * @return List of metrics attached to the provided detector uuid
     */
    @Query("select mmm.metric from MetricDetectorMapping mmm where mmm.detector.uuid = :uuid")
    List<Metric> findByDetectorUuid(@Param("uuid") String uuid);

    /**
     * Find a metric using metric id.
     * @param metricId
     * @return
     */
    Metric findById(@Param("metricId") Integer metricId);

    @Override
    Metric save(Metric metric);


}
