package com.expedia.adaptivealerting.modelservice.repo;

import com.expedia.adaptivealerting.modelservice.entity.Detector;
import com.expedia.adaptivealerting.modelservice.entity.Model;
import com.expedia.adaptivealerting.modelservice.entity.projection.InlineType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

/**
 * @author shsethi
 */
@RepositoryRestResource(excerptProjection = InlineType.class)
public interface DetectorRepository extends PagingAndSortingRepository<Detector, Long> {

    List<Model> findByDetectorId(@Param("detectorId") String detectorId);

    @Query("select mmm.detector from MetricDetectorMapping mmm where mmm.metric.hash = :hash")
    List<Model> findByMetricHash(@Param("hash") String hash);
}
