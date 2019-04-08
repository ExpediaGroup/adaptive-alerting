package com.expedia.adaptivealerting.modelservice.detectormapper.service;

import java.util.List;
import java.util.Map;

public interface DetectorMappingService {

    DetectorMatchResponse findMatchingDetectorMappings(List<Map<String, String>> tagsList);
    String createDetectorMapping(CreateDetectorMappingRequest createDetectorMappingRequest);
    void updateDetectorMapping(UpdateDetectorMappingRequest updateDetectorMappingRequest);
    void deleteDetectorMapping(String id);
    DetectorMapping findDetectorMapping(String id);
    List<DetectorMapping> search(SearchMappingsRequest searchMappingsRequest);
    List<DetectorMapping> findLastUpdated(int timeInSeconds);
    void disableDetectorMapping(String id);
}
