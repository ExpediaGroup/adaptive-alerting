package com.expedia.adaptivealerting.kafka.detectorrunner;

import com.expedia.adaptivealerting.anomdetect.detect.DetectorResult;
import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;
import com.expedia.metrics.MetricData;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class DetectorManager {

    @Autowired
    private DetectorRegistry detectorRegistry;

    public List<MappedMetricData> detect(ConsumerRecords<String, MappedMetricData> metricRecords) {
        log.info(String.valueOf(metricRecords.count()));
        List<MappedMetricData> mappedMetricDataList = new ArrayList<>();
        for (ConsumerRecord<String, MappedMetricData> consumerRecord : metricRecords) {
            MappedMetricData mappedMetricData = consumerRecord.value();
            if (mappedMetricData != null) {
                MetricData metricData = mappedMetricData.getMetricData();
                DetectorResult detectorResult = detectorRegistry.getDetector().detect(metricData);
                mappedMetricData.setAnomalyResult(detectorResult);
            }
            mappedMetricDataList.add(mappedMetricData);
        }

        return mappedMetricDataList;
    }

}
