package com.expedia.adaptivealerting.modelservice.detectormapper.service;

import lombok.Data;

import java.util.UUID;

@Data
public class SearchMappingsRequest {
    private String userId;
    private UUID detectorUuid;
}
