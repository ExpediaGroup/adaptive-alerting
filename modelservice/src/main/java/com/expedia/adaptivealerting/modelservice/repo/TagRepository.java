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
import com.expedia.adaptivealerting.modelservice.entity.Tag;
import com.expedia.adaptivealerting.modelservice.entity.projection.InlineType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

/**
 * Spring Data repository for tags.
 *
 * @author tbahl
 */
@RepositoryRestResource(excerptProjection = InlineType.class)
public interface TagRepository extends PagingAndSortingRepository<Tag, Long> {


    @Query(nativeQuery = true, value = "SELECT m.* FROM metric m INNER JOIN metric_tag_mapper mm on mm.metric_id = m.id  INNER JOIN tag t on t.id = mm.tag_id WHERE t.ukey = :ukey AND t.uvalue = :uvalue")
    List<Metric> findByTagContaining(@Param("ukey") String ukey, @Param("uvalue") String uvalue);


    /**
     * Finds a tag when matched with ukey and uvalue
     */
    List<Tag> findByUkeyContainingAndUvalueContaining(String ukey, String uvalue);


    @Override
    Tag save(Tag tag);

}
