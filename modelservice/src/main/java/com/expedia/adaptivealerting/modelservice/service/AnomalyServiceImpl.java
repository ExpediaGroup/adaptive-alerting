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
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.util.MetricUtil;
import com.expedia.adaptivealerting.modelservice.spi.*;
import com.expedia.adaptivealerting.modelservice.util.BeanUtil;
import com.expedia.adaptivealerting.modelservice.util.DetectorUtil;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service to fetch anomalies for a given metric and detector.
 */
@Service
@Slf4j
public class AnomalyServiceImpl implements AnomalyService {

    @Autowired
    @Qualifier("metricSourceServiceListFactoryBean")
    private List<?> metricSources;

    @Override
    public List<AnomalyResult> getAnomalies(AnomalyRequest request) {
        List<AnomalyResult> anomalyResults = new ArrayList<>();
        ((List<MetricSource>) metricSources)
                .forEach(metricSource -> {
                    List<MetricSourceResult> results = metricSource.getMetricData(request.getMetricTags());
                    Detector detector = DetectorUtil.getDetector(request.getDetectorType(), request.getDetectorParams());
                    for (MetricSourceResult result : results) {
                        MetricDefinition metricDefinition = MetricUtil.metricDefinition(null, null);
                        MetricData metricData = MetricUtil.metricData(metricDefinition, result.getDataPoint(), result.getEpochSecond());
                        AnomalyResult anomalyResult = detector.classify(metricData);
                        anomalyResults.add(anomalyResult);
                    }
                });
        return anomalyResults;

    }
}