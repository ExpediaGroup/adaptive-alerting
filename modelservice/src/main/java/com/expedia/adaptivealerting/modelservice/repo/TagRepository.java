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

import com.expedia.adaptivealerting.modelservice.entity.Tag;
import com.expedia.adaptivealerting.modelservice.entity.projection.InlineType;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

/**
 * Spring Data repository for tags.
 *
 * @author tbahl
 */
@RepositoryRestResource(excerptProjection = InlineType.class)
public interface TagRepository extends PagingAndSortingRepository<Tag, Long> {

    /**
     * Finds a tag when matched with ukey and uvalue
     */
    List<Tag> findByTagKeyContainsAndTagValueContains(String key, String value);

    Tag findFirstByTagKeyContainsAndTagValueContains(String key, String value);

    @Override
    Tag save(Tag tag);

}
