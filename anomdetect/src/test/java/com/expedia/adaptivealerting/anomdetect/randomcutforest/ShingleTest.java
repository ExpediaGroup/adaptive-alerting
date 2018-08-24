package com.expedia.adaptivealerting.anomdetect.randomcutforest;


import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.adaptivealerting.core.util.MetricUtil;
import com.expedia.www.haystack.commons.entities.MetricPoint;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.Iterator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ShingleTest {
    private Shingle shingle;
    private Iterator<String> iterator;

     @Before
    public void setUp() {
        shingle = null;
        iterator = null;
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


        shingle.offer(toMappedMPoint(MetricUtil.metricPoint(Instant.now().getEpochSecond(), Double.valueOf(1))));
        assertFalse(shingle.isReady());

        for (int i = 2; i <= 10; i++) {
            shingle.offer(toMappedMPoint(MetricUtil.metricPoint(Instant.now().getEpochSecond(), Double.valueOf(i))));
        }
        assertTrue(shingle.isReady());

        shingle.offer(toMappedMPoint(MetricUtil.metricPoint(Instant.now().getEpochSecond(), Double.valueOf(11))));
        assertTrue(shingle.isReady());
    }

    @Test
    public void toOutputFormatTest() {
        shingle = new Shingle();

        for (int i = 1; i <= 10; i++) {
            shingle.offer(toMappedMPoint(MetricUtil.metricPoint(Instant.now().getEpochSecond(), Double.valueOf(i))));
        }
        assertTrue(shingle.isReady());
        String out = shingle.toCsv().get();
        String expectedOut = "1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0,10.0";
        assertTrue(out.equals(expectedOut));
    }

    private MappedMetricData toMappedMPoint(MetricPoint metricPoint) {
        final MappedMetricData mappedMetricData = new MappedMetricData();
        mappedMetricData.setMetricData(MetricUtil.toMetricData(metricPoint));
        return mappedMetricData;
    }

}
