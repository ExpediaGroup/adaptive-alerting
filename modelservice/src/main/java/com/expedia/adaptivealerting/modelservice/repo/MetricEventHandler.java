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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@RepositoryEventHandler
public class MetricEventHandler {

    @Autowired
    MetricRepository metricRepository;

    @HandleBeforeCreate
    public void handleMetricCreate(Metric object) {

        // It would be nice to handle metrics with conflicting hashes with something like
        // `ON DUPLICATE KEY IGNORE`. This seems to be difficult to add to Spring Data REST
        // but might be worth re-visiting.
        // With this implementation we do have a potential race condition where a metric with
        // a matching hash could added after findByHash but before save.
        Object existingMetric = metricRepository.findByHash(object.getHash());
        if (existingMetric != null) {
            throw new ItemExistsException(existingMetric);
        }
        Set<Tag> tags = new HashSet<>();
        for (Map.Entry<String, Object> tagEntry : object.getTags().entrySet()) {
            Tag tag = new Tag();
            tag.setUkey(tagEntry.getKey());
            tag.setUvalue(tagEntry.getValue().toString());
            tags.add(tag);
        }
        object.setMetricTags(tags);

    }
}

