package com.expedia.adaptivealerting.anomdetect.arima;

/* This package uses https://github.com/Workday/timeseries-forecast Java open source library.
 * Adding the copyright notice and permission notice as required by the libary's license.

 * Copyright 2017 Workday, Inc.

 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

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

import static junit.framework.TestCase.assertEquals;

/**
 * @author ddivakar
 */
public class ArimaAnomalyDetectorTest {
    private static final int WARMUP_PERIOD = 15;

    // TODO This tolerance is very loose. Can we tighten it up? [WLW]
    private static final double TOLERANCE = 0.07;

    private UUID detectorUUID;
    private MetricDefinition metricDefinition;
    private long epochSecond;
    private static List<ArimaTestRow> data;


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
        final ListIterator<ArimaTestRow> testRows = data.listIterator();
        final int p = 3;
        final int d = 0;
        final int q = 3;
        final ArimaDetectorParams params = new ArimaDetectorParams()
                .setP(p)
                .setD(d)
                .setQ(q)
                .setWarmUpPeriod(WARMUP_PERIOD);
        final ArimaAnomalyDetector detector = new ArimaAnomalyDetector(detectorUUID, params);

        int noOfDataPoints = 1;
        while (testRows.hasNext()) {
            final ArimaTestRow testRow = testRows.next();
            final double observed = testRow.getObserved();
            final MetricData metricData = new MetricData(metricDefinition, observed, epochSecond);
            final AnomalyLevel level = detector.classify(metricData).getAnomalyLevel();

            if (noOfDataPoints < WARMUP_PERIOD) {
                assertEquals(AnomalyLevel.MODEL_WARMUP, level);
            }
            else {
                assertApproxEqual((testRow.getForecast()), detector.getTarget());
                assertEquals(AnomalyLevel.valueOf(testRow.getAnomalyLevel()), level);
            }

            noOfDataPoints += 1;
        }
      }

    private static void readDataFromCsv() {
        // example data set for ARIMA(3,0,3)
        final InputStream is = ClassLoader.getSystemResourceAsStream("tests/cal-inflow-tests-arima.csv");
        data = new CsvToBeanBuilder<ArimaTestRow>(new InputStreamReader(is))
                .withType(ArimaTestRow.class)
                .build()
                .parse();
    }

    private static void assertApproxEqual(double d1, double d2) {
        TestCase.assertTrue(MathUtil.isApproximatelyEqual(d1, d2, TOLERANCE));
    }
}