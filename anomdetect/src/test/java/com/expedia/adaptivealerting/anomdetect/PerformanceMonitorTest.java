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
package com.expedia.adaptivealerting.anomdetect;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ListIterator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.expedia.adaptivealerting.anomdetect.MonitoredDetector.PerfMonHandler;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.evaluator.RmseEvaluator;
import com.opencsv.bean.CsvToBeanBuilder;
import static org.junit.Assert.assertEquals;

/**
 * @author kashah
 *
 */
public class PerformanceMonitorTest {

    private static List<PerfMonitorTestRow> data;

    // Class under test
    private PerformanceMonitor perfMonitor;

    private static final double TOLERANCE = 0.01;
    private static final int MAX_TICKS = 100;

    @BeforeClass
    public static void setUpClass() throws IOException {
        readDataFromCsv();
    }

    @Before
    public void setUp() {
        this.perfMonitor = new PerformanceMonitor(new PerfMonHandler(), new RmseEvaluator(), MAX_TICKS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullListener() {
        new PerformanceMonitor(null, new RmseEvaluator(), 100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullEvaluator() {
        new PerformanceMonitor(new PerfMonHandler(), null, 100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_maxTicks() {
        new PerformanceMonitor(new PerfMonHandler(), new RmseEvaluator(), 0);
    }

    @Test
    public void testScore() {
        final ListIterator<PerfMonitorTestRow> testRows = data.listIterator();
        while (testRows.hasNext()) {
            final PerfMonitorTestRow testRow = testRows.next();
            final double observed = testRow.getObserved();
            final double predicted = testRow.getPredicted();

            AnomalyResult result = new AnomalyResult();
            result.setObserved(observed);
            result.setPredicted(predicted);
            double performanceScore = perfMonitor.evaluatePerformance(result);
            assertEquals(testRow.getScore(), performanceScore, TOLERANCE);
        }
    }

    private static void readDataFromCsv() {
        final InputStream is = ClassLoader.getSystemResourceAsStream("tests/perf-monitor-sample-input.csv");
        data = new CsvToBeanBuilder<PerfMonitorTestRow>(new InputStreamReader(is)).withType(PerfMonitorTestRow.class)
                .build().parse();
    }

}
