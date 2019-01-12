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

import com.expedia.adaptivealerting.modelservice.entity.MetricTagMapping;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data repository for MetricTagMapping.
 *
 * @author tbahl
 */
public interface MetricTagMappingRepository extends PagingAndSortingRepository<MetricTagMapping, Long> {

    /**
     * Finds metric_id on the basis of list of tags group by metric_id.
     * @param tagIds
     * @param tagSize
     * @return
     */
    @Query(nativeQuery = true, value = "SELECT metric_id FROM metric_tag_mapping WHERE tag_id IN :tagIds GROUP BY metric_id HAVING COUNT(distinct tag_id)=:tagSize")
    List<Integer> findById(@Param("tagIds")List<Long> tagIds, @Param("tagSize") Integer tagSize);

    @Override
    MetricTagMapping save(MetricTagMapping metricTagMapping);

}
