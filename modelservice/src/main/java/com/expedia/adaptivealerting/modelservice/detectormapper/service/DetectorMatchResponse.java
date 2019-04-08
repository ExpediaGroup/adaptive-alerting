package com.expedia.adaptivealerting.modelservice.detectormapper.service;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DetectorMatchResponse {
    private List<DetectorMapping> detectorMappings;
    private long lookupTimeInMillis;
}
