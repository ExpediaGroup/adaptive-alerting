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
package com.expedia.adaptivealerting.anomdetect.cusum;

import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyType;
import com.expedia.adaptivealerting.core.util.MathUtil;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.opencsv.bean.CsvToBeanBuilder;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;

public class CusumAnomalyDetectorTest {
    private static final double WEAK_SIGMAS = 3.0;
    private static final double STRONG_SIGMAS = 4.0;
    private static final double TOLERANCE = 0.01;
    private static final int WARMUP_PERIOD = 25;
    
    private UUID detectorUUID;
    private MetricDefinition metricDefinition;
    private long epochSecond;
    private static List<CusumTestRow> data;
    
    @BeforeClass
    public static void setUpClass() {
        readDataFromCsv();
    }
    
    @Before
    public void setUp() {
        this.detectorUUID = UUID.randomUUID();
        this.metricDefinition = new MetricDefinition("some-key");
        this.epochSecond = Instant.now().getEpochSecond();
    }
    
    @Test
    public void testEvaluate() {
        final ListIterator<CusumTestRow> testRows = data.listIterator();
        final CusumTestRow testRow0 = testRows.next();
        
        final CusumParams params = new CusumParams()
                .setType(AnomalyType.RIGHT_TAILED)
                .setTargetValue(0.16)
                .setWeakSigmas(WEAK_SIGMAS)
                .setStrongSigmas(STRONG_SIGMAS)
                .setSlackParam(0.5)
                .setInitMeanEstimate(testRow0.getObserved())
                .setWarmUpPeriod(WARMUP_PERIOD);
        final CusumAnomalyDetector detector = new CusumAnomalyDetector(detectorUUID, params);
        
        int numDataPoints = 1;
    
        while (testRows.hasNext()) {
            final CusumTestRow testRow = testRows.next();
            final double observed = testRow.getObserved();
            final MetricData metricData = new MetricData(metricDefinition, observed, epochSecond);
            final AnomalyLevel level = detector.classify(metricData).getAnomalyLevel();
            
            if (numDataPoints < WARMUP_PERIOD) {
                assertEquals(AnomalyLevel.MODEL_WARMUP, level);
            } else {
                assertApproxEqual(testRow.getSh(), detector.getSumHigh());
                assertApproxEqual(testRow.getSl(), detector.getSumLow());
                assertEquals(AnomalyLevel.valueOf(testRow.getLevel()), level);
            }
            numDataPoints++;
        }
    }
    
    private static void readDataFromCsv() {
        final InputStream is = ClassLoader.getSystemResourceAsStream("tests/cusum-sample-input.csv");
        data = new CsvToBeanBuilder<CusumTestRow>(new InputStreamReader(is))
                .withType(CusumTestRow.class)
                .build()
                .parse();
    }
    
    private static void assertApproxEqual(double d1, double d2) {
        TestCase.assertTrue(MathUtil.isApproximatelyEqual(d1, d2, TOLERANCE));
    }
}
