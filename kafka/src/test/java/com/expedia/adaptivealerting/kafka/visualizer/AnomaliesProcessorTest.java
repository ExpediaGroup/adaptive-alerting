package com.expedia.adaptivealerting.kafka.visualizer;

import com.expedia.adaptivealerting.anomdetect.detect.AnomalyLevel;
import com.expedia.adaptivealerting.anomdetect.detect.AnomalyThresholds;
import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.OutlierDetectorResult;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.TagCollection;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AnomaliesProcessorTest {

    private ExecutorService executorService;
    private AnomaliesProcessor anomaliesProcessor;

    @Before
    public void setUp() {
        executorService = mock(ExecutorService.class);
        when(executorService.submit(any(ElasticSearchBulkService.class))).thenReturn(null);
        anomaliesProcessor = new AnomaliesProcessor();
    }

    @Test
    public void testProcessMetrics() {
        ConsumerRecords<String, MappedMetricData> metricDataConsumerRecords = buildMetricRecords(2, AnomalyLevel.STRONG);
        List<AnomalyModel> anomalyModels = anomaliesProcessor.processMetrics(metricDataConsumerRecords, executorService);
        assertNotNull(anomalyModels);
        assertTrue(anomalyModels.size() == 2);
    }

    @Test
    public void testProcessZeroMetrics() {
        ConsumerRecords<String, MappedMetricData> metricDataConsumerRecords = buildMetricRecords(0, AnomalyLevel.WEAK);
        List<AnomalyModel> anomalyModels = anomaliesProcessor.processMetrics(metricDataConsumerRecords, executorService);
        assertNotNull(anomalyModels);
        assertTrue(anomalyModels.size() == 0);
    }

    @Test
    public void testProcessZeroMetricsForNormalAnomalies() {
        ConsumerRecords<String, MappedMetricData> metricDataConsumerRecords = buildMetricRecords(4, AnomalyLevel.NORMAL);
        List<AnomalyModel> anomalyModels = anomaliesProcessor.processMetrics(metricDataConsumerRecords, executorService);
        assertNotNull(anomalyModels);
        assertTrue(anomalyModels.size() == 0);
    }


    public static ConsumerRecords<String, MappedMetricData> buildMetricRecords(int no, AnomalyLevel anomalyLevel){
        TopicPartition topicPartition = new TopicPartition("test",2);
        List<ConsumerRecord<String,MappedMetricData>> consumerRecordList = new ArrayList<>();
        for (int i=0 ; i< no; i++) {
            consumerRecordList.add(buildConsumerRecord(anomalyLevel));
        }
        Map<TopicPartition, List<ConsumerRecord<String,MappedMetricData>>> topicPartitionListHashMap = new HashMap<>();
        topicPartitionListHashMap.put(topicPartition, consumerRecordList);
        ConsumerRecords<String,MappedMetricData> consumerRecords = new ConsumerRecords<>(topicPartitionListHashMap);
        return consumerRecords;
    }

    public static ConsumerRecord<String, MappedMetricData> buildConsumerRecord(AnomalyLevel anomalyLevel) {
        ConsumerRecord<String, MappedMetricData> consumerRecord = new ConsumerRecord("", 1 , 2L,
                "key",buildMappedMetricData(anomalyLevel));
        return consumerRecord;
    }

    public static MappedMetricData buildMappedMetricData(AnomalyLevel anomalyLevel){
        MappedMetricData mappedMetricData = new MappedMetricData();
        mappedMetricData.setDetectorUuid(UUID.randomUUID());
        mappedMetricData.setMetricData(buildMetricData());
        mappedMetricData.setAnomalyResult(buildOutlierDetectorResult(anomalyLevel));
        return mappedMetricData;
    }

    public static MetricData buildMetricData() {
        MetricData metricData = new MetricData(buildMetricDefinition(),1, 10000L);
        return metricData;
    }

    public static MetricDefinition buildMetricDefinition() {
        HashMap<String,String> tags = new HashMap<>();
        tags.put("test", "tag");
        TagCollection tagCollection = new TagCollection(tags);
        MetricDefinition metricDefinition = new MetricDefinition("test",tagCollection,tagCollection);
        return metricDefinition;
    }

    public static OutlierDetectorResult buildOutlierDetectorResult(AnomalyLevel anomalyLevel){
        OutlierDetectorResult outlierDetectorResult = new OutlierDetectorResult();
        outlierDetectorResult.setAnomalyLevel(anomalyLevel);
        outlierDetectorResult.setThresholds(buildAnomalyThresholds());
        return outlierDetectorResult;
    }

    public static AnomalyThresholds buildAnomalyThresholds() {
        return new AnomalyThresholds(20d,15d,10d,5d);
    }
}
