package com.expedia.adaptivealerting.modelservice.service;

import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;

import java.util.List;

public interface AnomalyService {

    List<AnomalyResult> getAnomalies(AnomalyRequest request);
}
