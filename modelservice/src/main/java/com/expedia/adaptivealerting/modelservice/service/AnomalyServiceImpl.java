/*
 * Copyright 2018-2019 Expedia Group, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expedia.adaptivealerting.modelservice.service;

import com.expedia.adaptivealerting.anomdetect.detector.*;
import com.expedia.adaptivealerting.anomdetect.comp.DetectorLookup;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.anomaly.AnomalyType;
import com.expedia.adaptivealerting.core.util.MetricUtil;
import com.expedia.adaptivealerting.core.util.ReflectionUtil;
import com.expedia.adaptivealerting.modelservice.spi.*;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service to fetch anomalies for a given metric and detector.
 */
@Service
@Slf4j
public class AnomalyServiceImpl implements AnomalyService {

    private final DetectorLookup detectorLookup = new DetectorLookup();

    @Autowired
    @Qualifier("metricSourceServiceListFactoryBean")
    private List<?> metricSources;

    @Override
    public List<AnomalyResult> getAnomalies(AnomalyRequest request) {
        return findAnomaliesInTrainingData(request);
    }

    private List<AnomalyResult> findAnomaliesInTrainingData(AnomalyRequest request) {
        List<AnomalyResult> anomalyResults = new ArrayList<>();
        ((List<MetricSource>) metricSources)
                .forEach(metricSource -> {
                    List<MetricSourceResult> results = metricSource.getMetricData(request.getMetricTags());
                    Detector detector = getDetector(request.getDetectorType(), request.getDetectorParams());
                    for (MetricSourceResult result : results) {
                        MetricData metricData = toMetricData(result);
                        AnomalyResult anomalyResult = detector.classify(metricData);
                        anomalyResults.add(anomalyResult);
                    }
                });
        return anomalyResults;
    }

    private Detector getDetector(String detectorType, Map paramsMap) {
        Class<? extends Detector> detectorClass = detectorLookup.getDetector(detectorType);
        AbstractDetector detector = (AbstractDetector) ReflectionUtil.newInstance(detectorClass);
        Class<? extends DetectorParams> paramsClass = detector.getParamsClass();
        DetectorParams params = (DetectorParams) new ObjectMapper().convertValue(paramsMap, paramsClass);
        detector.init(UUID.randomUUID(), params, doLegacyGetAnomalyType(params));
        return detector;
    }

    private MetricData toMetricData(MetricSourceResult result) {
        MetricDefinition metricDefinition = MetricUtil.metricDefinition(null, null);
        return MetricUtil.metricData(metricDefinition, result.getDataPoint(), result.getEpochSecond());
    }

    private AnomalyType doLegacyGetAnomalyType(DetectorParams params) {
        Class<? extends DetectorParams> paramsClass = params.getClass();

        if (ConstantThresholdParams.class.equals(paramsClass)) {
            return ((ConstantThresholdParams) params).getType();
        } else if (CusumParams.class.equals(paramsClass)) {
            return ((CusumParams) params).getType();
        } else {
            return AnomalyType.TWO_TAILED;
        }
    }
}