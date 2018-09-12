/*
 * Copyright 2018 Expedia Group, Inc.
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
package com.expedia.adaptivealerting.anomdetect.control;

import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
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

import static com.expedia.adaptivealerting.anomdetect.NSigmasClassifier.DEFAULT_STRONG_SIGMAS;
import static com.expedia.adaptivealerting.anomdetect.NSigmasClassifier.DEFAULT_WEAK_SIGMAS;
import static junit.framework.TestCase.assertEquals;

/**
 * @author kashah
 */
public class CusumAnomalyDetectorTest {
    private static final double WEAK_SIGMAS = DEFAULT_WEAK_SIGMAS;
    private static final double STRONG_SIGMAS = DEFAULT_STRONG_SIGMAS;
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
    public void testDefaultConstructor() {
        final CusumAnomalyDetector detector = new CusumAnomalyDetector(detectorUUID);
        assertEquals(WEAK_SIGMAS, detector.getWeakSigmas());
        assertEquals(STRONG_SIGMAS, detector.getStrongSigmas());
        assertEquals(WARMUP_PERIOD, detector.getWarmUpPeriod());
    }
    
    @Test
    public void testEvaluate() {
        final ListIterator<CusumTestRow> testRows = data.listIterator();
        final CusumTestRow testRow0 = testRows.next();
        final double observed0 = testRow0.getObserved();
        final double slackParam = 0.5;
        final CusumAnomalyDetector detector = new CusumAnomalyDetector(
                detectorUUID,
                CusumAnomalyDetector.Type.RIGHT_TAILED,
                observed0,
                slackParam,
                WARMUP_PERIOD,
                WEAK_SIGMAS,
                STRONG_SIGMAS,
                0.16);
        int noOfDataPoints = 1;
        
        assertEquals(WEAK_SIGMAS, detector.getWeakSigmas());
        assertEquals(STRONG_SIGMAS, detector.getStrongSigmas());
        assertEquals(WARMUP_PERIOD, detector.getWarmUpPeriod());
        
        while (testRows.hasNext()) {
            final CusumTestRow testRow = testRows.next();
            final double observed = testRow.getObserved();
            final MetricData metricData = new MetricData(metricDefinition, observed, epochSecond);
            final AnomalyLevel level = detector.classify(metricData).getAnomalyLevel();
            
            if (noOfDataPoints < WARMUP_PERIOD) {
                assertEquals(AnomalyLevel.UNKNOWN, level);
            } else {
                assertApproxEqual(testRow.getSh(), detector.getSumHigh());
                assertApproxEqual(testRow.getSl(), detector.getSumLow());
                assertEquals(AnomalyLevel.valueOf(testRow.getLevel()), level);
            }
            noOfDataPoints++;
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
