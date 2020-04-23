package com.expedia.adaptivealerting.kafka.visualizer;

import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;
import com.expedia.adaptivealerting.kafka.AnomalyVisualizer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.mock;

public class AnomaliesProcessorTest {

    private ExecutorService executorService;

    @Before
    public void setUp() {
        executorService = mock(ExecutorService.class);
    }

    @Test
    public void testProcessMetrics() {

    }

    public ConsumerRecords<String, MappedMetricData> buildMetricRecords(){
        return null;
    }

    public MappedMetricData buildMappedMetricData(){
        MappedMetricData mappedMetricData = new MappedMetricData();



        return mappedMetricData;
    }

}
