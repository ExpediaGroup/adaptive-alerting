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
package com.expedia.adaptivealerting.modelservice.repo.impl;

import com.expedia.adaptivealerting.anomdetect.detect.Detector;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.OutlierDetectorResult;
import com.expedia.adaptivealerting.anomdetect.source.DetectorDocument;
import com.expedia.adaptivealerting.anomdetect.source.DetectorFactory;
import com.expedia.adaptivealerting.anomdetect.util.MetricUtil;
import com.expedia.adaptivealerting.modelservice.metricsource.MetricSource;
import com.expedia.adaptivealerting.modelservice.repo.AnomalyRepository;
import com.expedia.adaptivealerting.modelservice.web.request.AnomalyRequest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Service to fetch anomalies for a given metric and detector.
 */
@Service
@Slf4j
public class AnomalyRepositoryImpl implements AnomalyRepository {

    @Autowired
    private List<? extends MetricSource> metricSources;

    @Autowired
    private DetectorFactory detectorFactory;

    @Override
    public List<OutlierDetectorResult> getAnomalies(AnomalyRequest request) {
        val metricDef = MetricUtil.metricDefinition();
        val detector = buildDetector(request);

        val anomalyResults = new ArrayList<OutlierDetectorResult>();
        metricSources.forEach(metricSource -> {
            val results = metricSource.getMetricData(request.getMetricTags());
            for (val result : results) {
                val metricData = MetricUtil.metricData(metricDef, result.getDataPoint(), result.getEpochSecond());
                val anomalyResult = (OutlierDetectorResult) detector.detect(metricData);
                anomalyResults.add(anomalyResult);
            }
        });
        return anomalyResults;
    }

    private Detector buildDetector(AnomalyRequest request) {
        val now = new Date();

        // TODO This would be better if we read the config itself from the request, rather than reading the params from
        //  the request. This is because the config includes hyperparams too. [WLW]
        val config = new HashMap<String, Object>();
        config.put("params", request.getDetectorParams());

        val document = new DetectorDocument()
                .setType(request.getDetectorType())
                .setUuid(request.getDetectorUuid())
                .setConfig(config)
                .setCreatedBy("adaptive-alerting")
                .setDateCreated(now)
                .setLastUpdateTimestamp(now);
        return detectorFactory.buildDetector(document);
    }
}
