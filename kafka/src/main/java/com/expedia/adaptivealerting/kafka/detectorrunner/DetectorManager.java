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

package com.expedia.adaptivealerting.kafka.detectorrunner;

import com.expedia.adaptivealerting.anomdetect.detect.DetectorResult;
import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;
import com.expedia.metrics.MetricData;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class DetectorManager {


    public List<MappedMetricData> detect(ConsumerRecords<String, MappedMetricData> metricRecords) {
        List<MappedMetricData> mappedMetricDataList = new ArrayList<>();
        for (ConsumerRecord<String, MappedMetricData> consumerRecord : metricRecords) {
            MappedMetricData mappedMetricData = consumerRecord.value();
            if (mappedMetricData != null) {
                MetricData metricData = mappedMetricData.getMetricData();
                SimpleDetector simpleDetector = new SimpleDetector();
                DetectorResult detectorResult = simpleDetector.detect(metricData);
                mappedMetricData.setAnomalyResult(detectorResult);
            }
            mappedMetricDataList.add(mappedMetricData);
        }
        return mappedMetricDataList;
    }

}
