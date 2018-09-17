package com.expedia.adaptivealerting.modelservice.repo;

import com.expedia.adaptivealerting.modelservice.entity.MetricModelMapping;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MetricModelMappingRepository extends PagingAndSortingRepository<MetricModelMapping, Long> {

    List<MetricModelMapping> findByMetricHash(@Param("hash") String hash);
}

