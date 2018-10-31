package com.expedia.adaptivealerting.modelservice.repo;

import com.expedia.adaptivealerting.modelservice.entity.Metric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

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
    }
}

