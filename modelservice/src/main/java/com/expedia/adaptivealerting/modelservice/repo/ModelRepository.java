/*
 * Copyright 2018 Expedia Group, Inc.
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

import com.expedia.adaptivealerting.modelservice.entity.Model;
import com.expedia.adaptivealerting.modelservice.entity.projection.ModelProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

/**
 * Spring Data repository for anomaly detection models.
 *
 * @author kashah
 */

@RepositoryRestResource(excerptProjection = ModelProjection.class)
public interface ModelRepository extends PagingAndSortingRepository<Model, Long> {

    List<Model> findByDetectorIdOrderByBuildTimestampDesc(@Param("detectorId") Long detectorId);

    @Query(nativeQuery = true, value = "SELECT *\n" +
            "FROM model m1\n" +
            "       join (SELECT model.detector_id, MAX(model.last_build_ts) max_last_build_ts\n" +
            "             FROM model\n" +
            "             where detector_id in(SELECT detector_id\n" +
            "                                  from metric_detector_mapping\n" +
            "                                  where metric_id =\n" +
            "                                        (SELECT m.id from metric m where hash = :hash))\n" +
            "             GROUP BY model.detector_id) filtered_table\n" +
            "where m1.detector_id = filtered_table.detector_id\n" +
            "  and m1.last_build_ts = filtered_table.max_last_build_ts;")
    List<Model> findByMetricHash(@Param("hash") String hash);

    List<Model> findTopByDetectorUuidOrderByBuildTimestampDesc(@Param("uuid") String uuid);

}
