package com.expedia.adaptivealerting.anomdetect.randomcutforest;


import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ShingleTest {
    private Shingle shingle;
    
    private MetricDefinition metricDefinition;
    private long epochSecond;
    private UUID uuid;
    
    @Before
    public void setUp() {
        shingle = null;
        
        this.metricDefinition = new MetricDefinition("some-key");
        this.epochSecond = Instant.now().getEpochSecond();
        this.uuid = UUID.randomUUID();
    }
    
    @Test
    public void emptyConstructorTest() {
        shingle = new Shingle();
        
        assertFalse(shingle.toValues().isPresent());
        assertFalse(shingle.isReady());
    }
    
    @Test
    public void isReadyTest() {
        shingle = new Shingle();
        
        shingle.offer(toMappedMetricData(epochSecond, 1.0));
        assertFalse(shingle.isReady());
        
        for (int i = 2; i <= 10; i++) {
            shingle.offer(toMappedMetricData(epochSecond, i));
        }
        assertTrue(shingle.isReady());
    
        shingle.offer(toMappedMetricData(epochSecond, 11.0));
        assertTrue(shingle.isReady());
    }
    
    @Test
    public void toOutputFormatTest() {
        shingle = new Shingle();
        
        for (int i = 1; i <= 10; i++) {
            shingle.offer(toMappedMetricData(epochSecond, i));
        }
        assertTrue(shingle.isReady());
        String out = shingle.toCsv().get();
        String expectedOut = "1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0,10.0";
        assertTrue(out.equals(expectedOut));
    }
    
    
    private MappedMetricData toMappedMetricData(long epochSecond, double value) {
        final MetricData metricData = new MetricData(metricDefinition, value, epochSecond);
        return new MappedMetricData(metricData, uuid, "pewma-detector");
    }
}
