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
package com.expedia.adaptivealerting.kafka.visualizer;

import com.expedia.adaptivealerting.anomdetect.detect.AnomalyLevel;
import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.OutlierDetectorResult;
import com.expedia.metrics.MetricData;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Slf4j
public class AnomaliesProcessor {

    public AnomaliesProcessor() {
    }

    public List<AnomalyModel> processMetrics(ConsumerRecords<String, MappedMetricData> metricRecords, ExecutorService executorService) {

        List<AnomalyModel> anomalyModels = new ArrayList();
        for (ConsumerRecord<String, MappedMetricData> consumerRecord : metricRecords) {
            MappedMetricData mappedMetricData = consumerRecord.value();
            if (mappedMetricData != null) {
                OutlierDetectorResult outlierDetectorResult = (OutlierDetectorResult) mappedMetricData.getAnomalyResult();
                if (outlierDetectorResult.getAnomalyLevel() != AnomalyLevel.NORMAL) {
                    AnomalyModel.Builder anomalyModel = AnomalyModel.newBuilder();
                    if (outlierDetectorResult != null) {
                        anomalyModel.level(outlierDetectorResult.getAnomalyLevel().toString());
                        anomalyModel.anomalyThresholds(outlierDetectorResult.getThresholds());
                    }

                    if (mappedMetricData.getDetectorUuid() != null) {
                        anomalyModel.uuid(mappedMetricData.getDetectorUuid().toString());
                    }
                    MetricData metricData = mappedMetricData.getMetricData();
                    if (metricData != null) {
                        anomalyModel.timestamp(VisualizerUtility.convertToDate(metricData.getTimestamp()));
                        anomalyModel.value(metricData.getValue());
                        if (metricData.getMetricDefinition() != null) {
                            anomalyModel.key(metricData.getMetricDefinition().getKey());
                            anomalyModel.tags(metricData.getMetricDefinition().getTags());
                        }
                    }
                    anomalyModels.add(anomalyModel.build());
                }
            }
        }
        if (anomalyModels.size() > 0) {
            ElasticSearchBulkService elasticSearchBulkService = new ElasticSearchBulkService(anomalyModels);
            executorService.submit(elasticSearchBulkService);
            log.info("sending anomaly records to elasticsearch: {}", anomalyModels.size());
        }
        return anomalyModels;
    }
}
