package com.expedia.adaptivealerting.modelservice.service;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;


@Data
public class AnomalyRequest {
    private String hash;
    private String detectorType;
    private Map<String, Object> detectorParams;
}