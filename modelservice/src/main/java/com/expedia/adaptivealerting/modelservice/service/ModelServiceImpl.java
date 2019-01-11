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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
        Metric m = metricRepository.findByHash(metric.getHash());
        if (m != null) {
            throw new ItemExistsException(m);
        }

        List<Tag> tagArrayList = new ArrayList<>();
        Metric newMetric = new Metric();
        Map<String, Object> tag = metric.getTags();
        Iterator<Map.Entry<String, Object>> tagiterator = tag.entrySet().iterator();

        for (Iterator<Map.Entry<String, Object>> iterator = tagiterator; iterator.hasNext(); ) {
            Map.Entry<String, Object> entry = iterator.next();
            metricRepository.save(metric);
            newMetric = metricRepository.findByHash(metric.getHash());
            tagArrayList = tagRepository.findByTagKeyContainingAndTagValueContaining(entry.getKey(), (String) entry.getValue());

            if (tagArrayList.size() == 0) tagRepository.save(new Tag(entry.getKey(), (String) entry.getValue()));
            metricTagMappingRepository.save(new MetricTagMapping(newMetric, tagArrayList.get(0)));
        }
        return newMetric;
    }

    @Override
    public List metricfinder(List<Tag> tagList){
        Map<String,Object> tagMap = null;
        List<Metric> metricList;
        Iterator iterator = tagList.iterator();
        String key = null;
        String value = null;
        while(iterator.hasNext()) {
            iterator.next();
            tagMap = tagList.stream().collect(Collectors.toMap(Tag::getTagKey, Tag::getTagValue, (oldValue, newValue)-> oldValue));
        }

        for (Map.Entry<String, Object> tag : tagMap.entrySet()) {
            key = tag.getKey();
            value = (String) tag.getValue();
        }
        metricList = metricRepository.findByTagContaining(key, (String) value, Pageable.unpaged());
        return metricList;
    }
}
