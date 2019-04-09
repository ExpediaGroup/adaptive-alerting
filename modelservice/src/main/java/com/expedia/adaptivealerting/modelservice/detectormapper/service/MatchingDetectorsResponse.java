package com.expedia.adaptivealerting.modelservice.detectormapper.service;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Data
@AllArgsConstructor
public class MatchingDetectorsResponse {
    private Map<Integer, List<Detector>> groupedDetectorsBySearchIndex;
    private long lookupTimeInMillis;

}