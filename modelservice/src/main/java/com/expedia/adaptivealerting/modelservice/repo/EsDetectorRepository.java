package com.expedia.adaptivealerting.modelservice.repo;

import com.expedia.adaptivealerting.modelservice.entity.ElasticsearchDetector;

import java.util.List;

public interface EsDetectorRepository {

    String createDetector(ElasticsearchDetector elasticsearchDetector);

    void deleteDetector(String uuid);

    void updateDetector(String uuid, ElasticsearchDetector elasticsearchDetector);

    List<ElasticsearchDetector> findByUuid(String uuid);

    List<ElasticsearchDetector> findByCreatedBy(String user);

    void toggleDetector(String uuid, Boolean enabled);

    List<ElasticsearchDetector> getLastUpdatedDetectors(String fromDate, String toDate);


}
