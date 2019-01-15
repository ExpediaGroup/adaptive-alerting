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
 * OnboardServiceDetail Implementation
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
        // insert metric
        Metric m = metricRepository.findByHash(metric.getHash());
        if (m != null) {
            throw new ItemExistsException(m);
        }
        metricRepository.save(metric);
        Metric newMetric = metricRepository.findByHash(metric.getHash());
        System.out.println("Entry Set for Tags: " + newMetric.getTags().entrySet());

        for (Iterator<Map.Entry<String, Object>> entryIterator = newMetric.getTags().entrySet().iterator(); entryIterator.hasNext(); ) {
            Map.Entry<String, Object> tag = entryIterator.next();
            String key = tag.getKey();
            String value = (String) tag.getValue();
            List<Tag> tagArrayList = tagRepository.findByTagKeyContainsAndTagValueContains(key, value);

            //insert tag
            if (tagArrayList.size() == 0) {
                tagRepository.save(new Tag(key, value));
                tagArrayList = tagRepository.findByTagKeyContainsAndTagValueContains(key, value);
            }
            metricTagMappingRepository.save(new MetricTagMapping(newMetric, tagArrayList.get(0)));
        }
        return newMetric;
    }

    @Override
    public List metricfinder(List<Tag> tagList) {
        List<Optional<Metric>> metricList= new ArrayList<>();
        List<Integer> IdList;

        Map<String,Object>tagMap = tagList.stream().collect(Collectors.toMap(Tag::getTagKey, Tag::getTagValue, (oldValue, newValue)-> oldValue));

        List<Long> tagIds = new ArrayList<>();

        for (Map.Entry<String, Object> tagEntry : tagMap.entrySet()) {
            String key = tagEntry.getKey();
            String value = (String) tagEntry.getValue();
            List<Tag> tag = tagRepository.findByTagKeyContainsAndTagValueContains(key, value);
            if(tag.size()==0){
                return metricList;
            }
            tagIds.add(tag.get(0).getId());
        }

        IdList = metricTagMappingRepository.findById(tagIds, tagIds.size());
        Iterator<Integer> iterator = IdList.iterator();

        while(iterator.hasNext()) {
            metricList.add(metricRepository.findById(Long.valueOf((iterator.next()))));
        }
        return metricList;
    }
}
