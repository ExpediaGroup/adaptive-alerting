package com.expedia.adaptivealerting.modelservice.repo;

import com.expedia.adaptivealerting.modelservice.entity.Detector;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface DetectorRepository extends ElasticsearchRepository<Detector, String> {

    Detector save(Detector detector);

    Detector findByUuid(String uuid);

    Iterable<Detector> findAll();

    List<Detector> findByMeta_CreatedBy(String user);

    List<Detector> findByMeta_DateLastUpdatedGreaterThan(String date);

    List<Detector> findByMeta_DateLastAccessedLessThan(String date);

    void deleteByUuid(String uuid);

    boolean existsById(String primaryKey);

}

