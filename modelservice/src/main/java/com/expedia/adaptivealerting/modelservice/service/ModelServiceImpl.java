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

import com.expedia.adaptivealerting.modelservice.entity.Metric;
import com.expedia.adaptivealerting.modelservice.entity.MetricTagMapping;
import com.expedia.adaptivealerting.modelservice.entity.Tag;
import com.expedia.adaptivealerting.modelservice.repo.ItemExistsException;
import com.expedia.adaptivealerting.modelservice.repo.MetricRepository;
import com.expedia.adaptivealerting.modelservice.repo.MetricTagMappingRepository;
import com.expedia.adaptivealerting.modelservice.repo.TagRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ModelServiceDetail Implementation
 *
 * @author tbahl
 */
@Service
@Slf4j
public class ModelServiceImpl implements ModelService {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private MetricRepository metricRepository;

    @Autowired
    private MetricTagMappingRepository metricTagMappingRepository;

    @Override
    public Metric onboard(Metric metric) {
        Metric m = metricRepository.findByHash(metric.getHash());
        if (m != null) {
            throw new ItemExistsException(m);
        }
        metricRepository.save(metric);
        Metric newMetric = metricRepository.findByHash(metric.getHash());

        for (Map.Entry<String, Object> tag : newMetric.getTags().entrySet()) {
            String key = tag.getKey();
            String value = (String) tag.getValue();

            Tag tagList = tagRepository.findFirstByTagKeyContainsAndTagValueContains(key, value);

            if (tagList == null) {
                tagRepository.save(new Tag(key, value));
            }
            tagList = tagRepository.findFirstByTagKeyContainsAndTagValueContains(key, value);
            metricTagMappingRepository.save(new MetricTagMapping(newMetric, tagList));
        }
        return newMetric;
    }

    @Override
    public List metricfinder(List<Tag> tagList) {
        List<Optional<Metric>> metricList = new ArrayList<>();
        List<Integer> IdList;

        List<Long> tagIds = new ArrayList<>();

        for (Tag tagEntry : tagList) {
            String key = tagEntry.getTagKey();
            String value = tagEntry.getTagValue();

            Tag tag = tagRepository.findFirstByTagKeyContainsAndTagValueContains(key, value);

            if (tag == null) {
                return metricList; // immediate returns an empty list if not found matching key value pair.
            }
            tagIds.add(tag.getId());
        }

        IdList = metricTagMappingRepository.findById(tagIds, tagIds.size());

        for (Integer integer : IdList) {
            metricList.add(metricRepository.findById(Long.valueOf((integer))));
        }
        return metricList;
    }
}