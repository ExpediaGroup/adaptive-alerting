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

import com.expedia.adaptivealerting.anomdetect.AnomalyDetector;
import com.expedia.adaptivealerting.anomdetect.constant.ConstantThresholdAnomalyDetector;
import com.expedia.adaptivealerting.anomdetect.constant.ConstantThresholdParams;
import com.expedia.adaptivealerting.anomdetect.cusum.CusumAnomalyDetector;
import com.expedia.adaptivealerting.anomdetect.cusum.CusumParams;
import com.expedia.adaptivealerting.anomdetect.ewma.EwmaAnomalyDetector;
import com.expedia.adaptivealerting.anomdetect.ewma.EwmaParams;
import com.expedia.adaptivealerting.anomdetect.pewma.PewmaAnomalyDetector;
import com.expedia.adaptivealerting.anomdetect.pewma.PewmaParams;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.anomaly.AnomalyThresholds;
import com.expedia.adaptivealerting.core.anomaly.AnomalyType;
import com.expedia.adaptivealerting.core.util.MetricUtil;
import com.expedia.adaptivealerting.modelservice.entity.Metric;
import com.expedia.adaptivealerting.modelservice.repo.MetricRepository;
import com.expedia.adaptivealerting.modelservice.spi.*;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author kashah
 */
@Service
@Slf4j
public class AnomalyService {

    @Autowired
    private MetricRepository metricRepository;

    @Autowired
    @Qualifier("metricSourceServiceListFactoryBean")
    private List metricSources;

    public List<ModifiedAnomalyResult> getAnomalies(AnomalyRequest request) {
        return findAnomaliesInTrainingData(request);
    }

    private List<ModifiedAnomalyResult> findAnomaliesInTrainingData(AnomalyRequest request) {

        List<ModifiedAnomalyResult> modifiedAnomalyResults = new ArrayList<>();
        Metric metric = metricRepository.findByHash(request.getHash());

        ((List<MetricSource>) metricSources)
                .forEach(metricSource -> {
                    List<MetricSourceResult> results = metricSource.getMetricData(metric.getKey());
                    AnomalyDetector detector = getDetector(request.getDetectorType(), request.getDetectorParams());
                    for (MetricSourceResult result : results) {
                        MetricData metricData = toMetricData(result);
                        AnomalyResult anomalyResult = detector.classify(metricData);
                        modifiedAnomalyResults.add(new ModifiedAnomalyResult(metricData.getTimestamp(), anomalyResult.getAnomalyLevel(), metricData.getValue(), anomalyResult.getPredicted()));
                    }
                });
        return modifiedAnomalyResults;
    }

    private AnomalyDetector getDetector(String detectorType, Map paramsMap) {
        AnomalyType type = getAnomalyType((String) paramsMap.get("type"));
        Double strongSigmas = (Double) paramsMap.get("strongSigmas");
        Double weakSigmas = (Double) paramsMap.get("weakSigmas");

        switch (detectorType) {
            case "constant-detector":
                Double upperStrong = (Double) paramsMap.get("upperStrong");
                Double lowerStrong = (Double) paramsMap.get("lowerStrong");
                Double upperWeak = (Double) paramsMap.get("upperWeak");
                Double lowerWeak = (Double) paramsMap.get("lowerWeak");

                ConstantThresholdParams params = new ConstantThresholdParams()
                        .setType(type)
                        .setThresholds(new AnomalyThresholds(upperStrong, upperWeak, lowerStrong, lowerWeak));
                return new ConstantThresholdAnomalyDetector(params);

            case "ewma-detector":
                EwmaParams ewmaParams = new EwmaParams()
                        .setWeakSigmas(weakSigmas)
                        .setStrongSigmas(strongSigmas);
                return new EwmaAnomalyDetector(ewmaParams);

            case "pewma-detector":
                PewmaParams pewmaParams = new PewmaParams()
                        .setWeakSigmas(weakSigmas)
                        .setStrongSigmas(strongSigmas);
                return new PewmaAnomalyDetector(pewmaParams);

            case "cusum-detector":
                Integer warmUpPeriod = (Integer) paramsMap.get("warmUpPeriod");
                Double targetValue = (Double) paramsMap.get("targetValue");
                CusumParams cusumParams = new CusumParams()
                        .setType(type)
                        .setTargetValue(targetValue)
                        .setWarmUpPeriod(warmUpPeriod);
                return new CusumAnomalyDetector(cusumParams);

            default:
                throw new IllegalStateException("Illegal detector type: " + detectorType);
        }
    }

    private AnomalyType getAnomalyType(String anomalyType) {
        if (anomalyType == null) {
            return null;
        } else {
            switch (anomalyType) {
                case "LEFT_TAILED":
                    return AnomalyType.LEFT_TAILED;
                case "RIGHT_TAILED":
                    return AnomalyType.RIGHT_TAILED;
                case "TWO_TAILED":
                    return AnomalyType.TWO_TAILED;
                default:
                    throw new IllegalStateException("Illegal anomaly type: " + anomalyType);
            }
        }
    }

    private MetricData toMetricData(MetricSourceResult result) {
        MetricDefinition metricDefinition = MetricUtil.metricDefinition(null, null);
        return MetricUtil.metricData(metricDefinition, result.getDataPoint(), result.getEpochSecond());
    }
}