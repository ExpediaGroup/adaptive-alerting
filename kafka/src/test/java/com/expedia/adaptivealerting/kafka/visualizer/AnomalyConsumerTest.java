package com.expedia.adaptivealerting.kafka.visualizer;

import com.expedia.adaptivealerting.anomdetect.detect.AnomalyLevel;
import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AnomalyConsumerTest {

    private KafkaConsumer<String, MappedMetricData> kafkaConsumer;
    private ExecutorService executorService;
    private AnomalyConsumer anomalyConsumer;


    @Before
    public void setUp() {
        kafkaConsumer = mock(KafkaConsumer.class);
        executorService = mock(ExecutorService.class);
        when(executorService.submit(any(ElasticSearchBulkService.class))).thenReturn(null);
        anomalyConsumer = new AnomalyConsumer();
        anomalyConsumer.setKafkaConsumer(kafkaConsumer);
        anomalyConsumer.setExecutorService(executorService);
    }

    @Test
    public void testProcess() {
        ConsumerRecords<String, MappedMetricData> metricRecords = AnomaliesProcessorTest.buildMetricRecords(2,
                AnomalyLevel.STRONG);
        when(kafkaConsumer.poll(anyLong())).thenReturn(metricRecords);
        assertTrue(anomalyConsumer.process(kafkaConsumer, true));
    }

    @Test
    public void testProcessZeroMetrics() {
        ConsumerRecords<String, MappedMetricData> metricRecords = AnomaliesProcessorTest.buildMetricRecords(0,
                AnomalyLevel.STRONG);
        when(kafkaConsumer.poll(anyLong())).thenReturn(metricRecords);
        assertTrue(anomalyConsumer.process(kafkaConsumer, true));

    }

    @Test
    public void testProcessWakeException() {
        when(kafkaConsumer.poll(anyLong())).thenThrow(WakeupException.class);
        assertFalse(anomalyConsumer.process(kafkaConsumer, true));
        verify(kafkaConsumer, times(1)).close();

    }

    @Test
    public void testProcessException() {
        when(kafkaConsumer.poll(anyLong())).thenThrow(IllegalStateException.class);
        assertTrue(anomalyConsumer.process(kafkaConsumer, true));

    }
}
