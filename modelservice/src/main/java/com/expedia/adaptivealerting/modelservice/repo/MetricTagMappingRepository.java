package com.expedia.adaptivealerting.modelservice.repo;

import com.expedia.adaptivealerting.modelservice.entity.MetricTagMapping;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Spring Data repository for MetricTagMapping.
 *
 * @author tbahl
 */
public interface MetricTagMappingRepository extends PagingAndSortingRepository<MetricTagMapping, Long> {
    @Override
    MetricTagMapping save(MetricTagMapping metricTagMapping);

}
