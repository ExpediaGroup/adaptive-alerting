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
package com.expedia.adaptivealerting.anomdetect.rcf;

import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ShingleTest {
    private Shingle shingle;
    
    private MetricDefinition metricDefinition;
    private long epochSecond;
    
    @Before
    public void setUp() {
        shingle = null;
        
        this.metricDefinition = new MetricDefinition("some-key");
        this.epochSecond = Instant.now().getEpochSecond();
    }
    
    @Test
    public void emptyConstructorTest() {
        shingle = new Shingle();
        
        assertFalse(shingle.isReady());
    }
    
    @Test
    public void isReadyTest() {
        shingle = new Shingle();
        
        shingle.offer(toMetricData(epochSecond, 1.0));
        assertFalse(shingle.isReady());
        
        for (int i = 2; i <= 10; i++) {
            shingle.offer(toMetricData(epochSecond, i));
        }
        assertTrue(shingle.isReady());
        
        shingle.offer(toMetricData(epochSecond, 11.0));
        assertTrue(shingle.isReady());
    }
    
    @Test
    public void toOutputFormatTest() {
        shingle = new Shingle();
        
        for (int i = 1; i <= 10; i++) {
            shingle.offer(toMetricData(epochSecond, i));
        }
        assertTrue(shingle.isReady());
        String out = shingle.toCsv().get();
        String expectedOut = "1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0,10.0";
        assertTrue(out.equals(expectedOut));
    }
    
    private MetricData toMetricData(long epochSecond, double value) {
        return new MetricData(metricDefinition, value, epochSecond);
    }
}
