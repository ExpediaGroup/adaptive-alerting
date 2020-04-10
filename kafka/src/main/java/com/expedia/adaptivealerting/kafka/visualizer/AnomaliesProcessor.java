package com.expedia.adaptivealerting.kafka.visualizer;

import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.OutlierDetectorResult;
import com.expedia.metrics.MetricData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class AnomaliesProcessor {

    private ElasticSearchService elasticSearchService;

    public AnomaliesProcessor(ElasticSearchService elasticSearchService) {
        this.elasticSearchService = elasticSearchService;
    }

    public void processMetrics(ConsumerRecords<String, MappedMetricData> metricRecords) {
        for (ConsumerRecord<String, MappedMetricData> consumerRecord : metricRecords) {
            log.info("consumer record: " + consumerRecord.value().getMetricData() + " " + consumerRecord.value().getDetectorUuid()
                    + " " + consumerRecord.value().getAnomalyResult().getAnomalyLevel());
            AnomalyModel anomalyModel = new AnomalyModel();
            MappedMetricData mappedMetricData = consumerRecord.value();
            if (mappedMetricData != null) {
                if (mappedMetricData.getDetectorUuid() != null){
                    anomalyModel.setUuid(mappedMetricData.getDetectorUuid().toString());
                }
                MetricData metricData = mappedMetricData.getMetricData();
                if (metricData != null) {
                    anomalyModel.setTimestamp(new Date(metricData.getTimestamp() * 1000L));
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
            String json = convertToJson(anomalyModel);
            log.info(String.valueOf(json.length()));

            elasticSearchService.execute(json);
        }
    }

    private static String convertToJson(Object object) {
        String json = "";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            json = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }
}
