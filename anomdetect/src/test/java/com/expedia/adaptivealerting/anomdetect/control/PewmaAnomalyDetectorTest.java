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
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBeanBuilder;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import static java.lang.Math.sqrt;
import static junit.framework.TestCase.assertEquals;

/**
 * @author David Sutherland
 */
public class PewmaAnomalyDetectorTest {
    private static final double WEAK_SIGMAS = 2.0;
    private static final double STRONG_SIGMAS = 3.0;
    private static final double DEFAULT_ALPHA = 0.05;
    private static final double TOLERANCE = 0.00001;
    
    private static final String SAMPLE_INPUT_PATH = "tests/pewma-sample-input.csv";
    private static final String CAL_INFLOW_PATH = "tests/cal-inflow-tests-pewma.csv";
    
    private UUID detectorUUID;
    private MetricDefinition metricDefinition;
    private long epochSecond;
    
    @Before
    public void setUp() {
        this.detectorUUID = UUID.randomUUID();
        this.metricDefinition = new MetricDefinition("some-key");
        this.epochSecond = Instant.now().getEpochSecond();
    }
    
    @Test
    public void testDefaultConstructor() {
        final PewmaAnomalyDetector detector = new PewmaAnomalyDetector(detectorUUID);
        assertEquals(0.0, detector.getMean());
    }
    
    @Test
    public void pewmaCloseToEwmaWithZeroBeta() throws IOException {
        double beta = 0.0;
        
        final ListIterator<String[]> testRows = readData_sampleInput().listIterator();
        final Double initialValue = Double.parseDouble(testRows.next()[0]);
        final PewmaAnomalyDetector pewmaOutlierDetector = new PewmaAnomalyDetector(
                detectorUUID, DEFAULT_ALPHA, beta, STRONG_SIGMAS, WEAK_SIGMAS, initialValue);
        final EwmaAnomalyDetector ewmaOutlierDetector = new EwmaAnomalyDetector(
                detectorUUID, DEFAULT_ALPHA, STRONG_SIGMAS, WEAK_SIGMAS, initialValue);
        
        int rowCount = 1;
        while (testRows.hasNext()) {
            final Float observed = Float.parseFloat(testRows.next()[0]);
            
            double ewmaStdDev = sqrt(ewmaOutlierDetector.getVariance());
            
            double threshold = 1.0 / rowCount; // results converge with more iterations
            assertApproxEqual(ewmaOutlierDetector.getMean(), pewmaOutlierDetector.getMean(), threshold);
            assertApproxEqual(ewmaStdDev, pewmaOutlierDetector.getStdDev(), threshold);
            
            final MetricData metricData = new MetricData(metricDefinition, observed, epochSecond);
            final AnomalyLevel pewmaLevel = pewmaOutlierDetector.classify(metricData).getAnomalyLevel();
            final AnomalyLevel ewmaLevel = ewmaOutlierDetector.classify(metricData).getAnomalyLevel();
            
            if (rowCount > PewmaAnomalyDetector.DEFAULT_TRAINING_LENGTH) {
                assertEquals(pewmaLevel, ewmaLevel);
            }
            rowCount++;
        }
    }
    
    @Test
    public void evaluate() {
        double beta = 0.5;
        
        final ListIterator<PewmaTestRow> testRows = readData_calInflow().listIterator();
        final PewmaTestRow testRow0 = testRows.next();
        final PewmaAnomalyDetector detector = new PewmaAnomalyDetector(
                detectorUUID, DEFAULT_ALPHA, beta, STRONG_SIGMAS, WEAK_SIGMAS, testRow0.getObserved());
        
        while (testRows.hasNext()) {
            final PewmaTestRow testRow = testRows.next();
            
            final double observed = testRow.getObserved();
            
            final MetricData metricData = new MetricData(metricDefinition, observed, epochSecond);
            final AnomalyLevel level = detector.classify(metricData).getAnomalyLevel();
            
            assertApproxEqual(testRow.getMean(), detector.getMean(), TOLERANCE);
            assertApproxEqual(testRow.getStd(), detector.getStdDev(), TOLERANCE);
            assertEquals(AnomalyLevel.valueOf(testRow.getLevel()), level);
        }
    }
    
    private static List<String[]> readData_sampleInput() throws IOException {
        final InputStream is = ClassLoader.getSystemResourceAsStream(SAMPLE_INPUT_PATH);
        CSVReader reader = new CSVReader(new InputStreamReader(is));
        return reader.readAll();
    }
    
    private static List<PewmaTestRow> readData_calInflow() {
        final InputStream is = ClassLoader.getSystemResourceAsStream(CAL_INFLOW_PATH);
        return new CsvToBeanBuilder<PewmaTestRow>(new InputStreamReader(is))
                .withType(PewmaTestRow.class)
                .build()
                .parse();
    }
    
    private static void assertApproxEqual(double d1, double d2, double tolerance) {
        TestCase.assertTrue(d1 + " !~ " + d2, MathUtil.isApproximatelyEqual(d1, d2, tolerance));
    }
}
