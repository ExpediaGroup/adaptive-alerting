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

