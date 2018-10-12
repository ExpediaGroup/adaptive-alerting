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

import com.expedia.adaptivealerting.modelservice.entity.Metric;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

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
}
