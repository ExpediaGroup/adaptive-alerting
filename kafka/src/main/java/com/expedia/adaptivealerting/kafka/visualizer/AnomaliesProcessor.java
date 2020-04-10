package com.expedia.adaptivealerting.kafka.visualizer;

import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.OutlierDetectorResult;
import com.expedia.metrics.MetricData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Component
@Slf4j
public class AnomaliesProcessor {

    private ElasticSearchService elasticSearchService;

    public AnomaliesProcessor(ElasticSearchService elasticSearchService) {
        this.elasticSearchService = elasticSearchService;
    }

    public void processMetrics(ConsumerRecords<String, MappedMetricData> metricRecords) throws IOException {
        for (ConsumerRecord<String, MappedMetricData> consumerRecord : metricRecords) {
            log.info("consumer record: " + consumerRecord.value().getMetricData() + " " + consumerRecord.value().getDetectorUuid()
                    + " " + consumerRecord.value().getAnomalyResult().getAnomalyLevel());
            MappedMetricData mappedMetricData = consumerRecord.value();
            MetricData metricData = mappedMetricData.getMetricData();
            AnomalyModel anomalyModel = new AnomalyModel();
            if (metricData != null ) {
                anomalyModel.setKey(metricData.getMetricDefinition().getKey());
                anomalyModel.setTimestamp(new Date(metricData.getTimestamp()*1000L));
                anomalyModel.setTags(metricData.getMetricDefinition().getTags());
            }
            OutlierDetectorResult outlierDetectorResult = (OutlierDetectorResult) mappedMetricData.getAnomalyResult();
            anomalyModel.setLevel(outlierDetectorResult.getAnomalyLevel().toString());
            anomalyModel.setAnomalyThresholds(outlierDetectorResult.getThresholds());
            anomalyModel.setUuid(mappedMetricData.getDetectorUuid().toString());
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
