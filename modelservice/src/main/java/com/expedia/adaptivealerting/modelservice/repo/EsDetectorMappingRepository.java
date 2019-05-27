package com.expedia.adaptivealerting.modelservice.repo;

import com.expedia.adaptivealerting.modelservice.dto.detectormapping.CreateDetectorMappingRequest;
import com.expedia.adaptivealerting.modelservice.entity.ElasticsearchDetectorMapping;
import com.expedia.adaptivealerting.modelservice.dto.detectormapping.MatchingDetectorsResponse;
import com.expedia.adaptivealerting.modelservice.dto.detectormapping.SearchMappingsRequest;

import java.util.List;
import java.util.Map;

public interface EsDetectorMappingRepository {

    MatchingDetectorsResponse findMatchingDetectorMappings(List<Map<String, String>> tagsList);

    String createDetectorMapping(CreateDetectorMappingRequest createDetectorMappingRequest);

    void deleteDetectorMapping(String id);

    ElasticsearchDetectorMapping findDetectorMapping(String id);

    List<ElasticsearchDetectorMapping> search(SearchMappingsRequest searchMappingsRequest);

    List<ElasticsearchDetectorMapping> findLastUpdated(int timeInSeconds);

    void disableDetectorMapping(String id);
}
