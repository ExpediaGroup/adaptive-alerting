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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * OnboardServiceDetail Implementation
 *
 * @author tbahl
 */
@Service
@Slf4j
public class OnboardServiceImpl implements OnboardService {

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

        List<Tag> tagList = new ArrayList<>();
        Metric metricEntry = new Metric();
        Tag tagEntry = new Tag();
        Map<String, Object> tag = metric.getTags();
        Iterator<Map.Entry<String, Object>> tagiterator = tag.entrySet().iterator();

        for(Iterator<Map.Entry<String, Object>> iterator = tagiterator;  iterator.hasNext();) {
            Map.Entry<String, Object> entry = iterator.next();
            metricRepository.save(metric);
            metricEntry = metricRepository.findByHash(metric.getHash());
            tagList = tagRepository.findByUkeyContainingAndUvalueContaining(entry.getKey(), (String) entry.getValue());

            if (tagList.size() == 0) {
                tagEntry = tagRepository.save(new Tag(entry.getKey(), (String) entry.getValue()));
                metricTagMappingRepository.save(new MetricTagMapping(metricEntry, tagEntry));
            }
            metricTagMappingRepository.save(new MetricTagMapping(metricEntry, tagList.get(0)));
        }
        return metricEntry;
    }
}
