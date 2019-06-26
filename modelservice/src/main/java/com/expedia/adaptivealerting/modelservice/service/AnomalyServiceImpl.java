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

import com.expedia.adaptivealerting.anomdetect.Detector;
import com.expedia.adaptivealerting.anomdetect.detectorsource.DetectorDocument;
import com.expedia.adaptivealerting.anomdetect.outlier.AnomalyResult;
import com.expedia.adaptivealerting.anomdetect.detectorsource.legacy.LegacyDetectorFactory;
import com.expedia.adaptivealerting.anomdetect.util.MetricUtil;
import com.expedia.adaptivealerting.modelservice.spi.MetricSource;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service to fetch anomalies for a given metric and detector.
 */
@Service
@Slf4j
public class AnomalyServiceImpl implements AnomalyService {

    @Autowired
    private List<? extends MetricSource> metricSources;

    @Override
    public List<AnomalyResult> getAnomalies(AnomalyRequest request) {
        val metricDef = MetricUtil.metricDefinition();
        val detector = getDetector(request);

        val anomalyResults = new ArrayList<AnomalyResult>();
        metricSources.forEach(metricSource -> {
            val results = metricSource.getMetricData(request.getMetricTags());
            for (val result : results) {
                val metricData = MetricUtil.metricData(metricDef, result.getDataPoint(), result.getEpochSecond());
                val anomalyResult = (AnomalyResult) detector.detect(metricData);
                anomalyResults.add(anomalyResult);
            }
        });
        return anomalyResults;
    }

    private Detector getDetector(AnomalyRequest request) {
        val legacyDetectorType = request.getDetectorType();
        val paramsMap = request.getDetectorParams();
        Map detectorConfig = new HashMap<>();
        detectorConfig.put("params", paramsMap);
        val detector = new DetectorDocument()
                .setType(legacyDetectorType)
                .setCreatedBy("adaptive-alerting")
                .setLastUpdateTimestamp(new Date())
                .setDetectorConfig(detectorConfig);
        return new LegacyDetectorFactory().createDetector(UUID.randomUUID(), detector);
    }
}
