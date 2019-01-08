package com.expedia.adaptivealerting.modelservice.service;

import com.expedia.adaptivealerting.modelservice.entity.Metric;
import com.expedia.adaptivealerting.modelservice.entity.MetricTagMapping;
import com.expedia.adaptivealerting.modelservice.entity.Tag;
import com.expedia.adaptivealerting.modelservice.repo.MetricRepository;
import com.expedia.adaptivealerting.modelservice.repo.MetricTagMappingRepository;
import com.expedia.adaptivealerting.modelservice.repo.TagRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * OnboardService Implementation
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

    private String hashval = " ";

    @Override
    public Boolean isOnboarded(Metric metric) {
        String hash = metric.getHash();
        Metric m = metricRepository.findByHash(hash);
        hashval = String.valueOf(m);
        if (hashval.equals("null")) {
            return false;
        }
        return true;
    }

    @Override
    public Integer onboard(Metric metric) {
        List<Tag> tagList;
        Metric metricEntry;
        Tag tagEntry;
        Integer metricID = 0;


        for (Iterator<Map.Entry<String, Object>> tagIterator = metric.getTags().entrySet().iterator(); tagIterator.hasNext(); ) {
            Map.Entry<String, Object> entry = tagIterator.next();

            metricRepository.save(metric);
            metricEntry = metricRepository.findByHash(metric.getHash());

            tagList = tagRepository.findByUkeyContainingAndUvalueContaining(entry.getKey(), (String) entry.getValue());

            if (tagList.size() == 0) {
                tagEntry = tagRepository.save(new Tag(null, entry.getKey(), (String) entry.getValue()));
                metricTagMappingRepository.save(new MetricTagMapping(null, metricEntry, tagEntry));
                metricID = Math.toIntExact(metricEntry.getId());
            } else {

                metricTagMappingRepository.save(new MetricTagMapping(null, metricEntry, tagList.get(0)));

            }

        }
        return metricID;


    }

}