package com.expedia.adaptivealerting.kafka.visualizer;

import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.OutlierDetectorResult;
import com.expedia.metrics.MetricData;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Slf4j
public class AnomaliesProcessor {

    public AnomaliesProcessor() {
    }

    public void processMetrics(ConsumerRecords<String, MappedMetricData> metricRecords, ExecutorService executorService) {

        List<AnomalyModel> anomalyModels = new ArrayList();
        for (ConsumerRecord<String, MappedMetricData> consumerRecord : metricRecords) {
            AnomalyModel anomalyModel = new AnomalyModel();
            MappedMetricData mappedMetricData = consumerRecord.value();
            if (mappedMetricData != null) {
                if (mappedMetricData.getDetectorUuid() != null) {
                    anomalyModel.setUuid(mappedMetricData.getDetectorUuid().toString());
                }
                MetricData metricData = mappedMetricData.getMetricData();
                if (metricData != null) {
                    anomalyModel.setTimestamp(Utility.convertToDate(metricData.getTimestamp()));
                    anomalyModel.setValue(metricData.getValue());
                    if (metricData.getMetricDefinition() != null) {
                        anomalyModel.setKey(metricData.getMetricDefinition().getKey());
                        anomalyModel.setTags(metricData.getMetricDefinition().getTags());
                    }
                }
                OutlierDetectorResult outlierDetectorResult = (OutlierDetectorResult) mappedMetricData.getAnomalyResult();
                if (outlierDetectorResult != null) {
                    anomalyModel.setLevel(outlierDetectorResult.getAnomalyLevel().toString());
                    anomalyModel.setAnomalyThresholds(outlierDetectorResult.getThresholds());
                }
            }
            anomalyModels.add(anomalyModel);
        }
        if (anomalyModels.size() > 0) {
            ElasticSearchBulkService elasticSearchBulkService = new ElasticSearchBulkService(anomalyModels);
            executorService.submit(elasticSearchBulkService);
            log.info("sending anomaly records to elasticsearch: {}", anomalyModels.size());
        }
    }
}
