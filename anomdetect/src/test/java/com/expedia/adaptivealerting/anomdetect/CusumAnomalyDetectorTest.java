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

import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.util.MathUtil;
import com.expedia.adaptivealerting.core.util.MetricPointUtil;
import com.opencsv.bean.CsvToBeanBuilder;
import junit.framework.TestCase;
import org.junit.BeforeClass;
import org.junit.Test;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.List;
import java.util.ListIterator;
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

    private static List<CusumTestRow> data;

    @BeforeClass
    public static void setUpClass() throws IOException {
        readDataFromCsv();
    }

    @Test
    public void testDefaultConstructor() {
        final CusumAnomalyDetector detector = new CusumAnomalyDetector();
        assertEquals(WEAK_SIGMAS, detector.getWeakThresholdSigmas());
        assertEquals(STRONG_SIGMAS, detector.getStrongThresholdSigmas());
        assertEquals(WARMUP_PERIOD, detector.getWarmUpPeriod());
    }

    @Test(expected = IllegalStateException.class)
    public void testIllegalTailException() {
        final int tail = 4;
        final double observed = 4.0;
        final int warmUpPeriod = 0;
        final double slackParam = 0.5;
        new CusumAnomalyDetector(tail, 10, slackParam, warmUpPeriod, WEAK_SIGMAS, STRONG_SIGMAS, 0.16)
                .classify(MetricPointUtil.metricPoint(Instant.now(), observed));
    }

    @Test
    public void testEvaluate() {

        final ListIterator<CusumTestRow> testRows = data.listIterator();
        final CusumTestRow testRow0 = testRows.next();
        final double observed0 = testRow0.getObserved();
        final double slackParam = 0.5;

        final CusumAnomalyDetector detector = new CusumAnomalyDetector(1, observed0, slackParam, WARMUP_PERIOD,
                WEAK_SIGMAS, STRONG_SIGMAS, 0.16);
        int noOfDataPoints = 1;

        // Params
        assertEquals(WEAK_SIGMAS, detector.getWeakThresholdSigmas());
        assertEquals(STRONG_SIGMAS, detector.getStrongThresholdSigmas());
        assertEquals(WARMUP_PERIOD, detector.getWarmUpPeriod());

        while (testRows.hasNext()) {
            final CusumTestRow testRow = testRows.next();
            final double observed = testRow.getObserved();
            AnomalyResult result = detector.classify(MetricPointUtil.metricPoint(Instant.now(), observed));

            if (noOfDataPoints < WARMUP_PERIOD) {
                assertEquals(AnomalyLevel.valueOf("UNKNOWN"), result.getAnomalyLevel());
            } else {
                assertApproxEqual(testRow.getSh(), detector.getSumHigh());
                assertApproxEqual(testRow.getSl(), detector.getSumLow());
                assertEquals(AnomalyLevel.valueOf(testRow.getLevel()), result.getAnomalyLevel());
            }
            noOfDataPoints += 1;
        }
    }

    private static void readDataFromCsv() {
        final InputStream is = ClassLoader.getSystemResourceAsStream("tests/cusum-sample-input.csv");
        data = new CsvToBeanBuilder<CusumTestRow>(new InputStreamReader(is)).withType(CusumTestRow.class).build()
                .parse();

    }

    private static void assertApproxEqual(double d1, double d2) {
        TestCase.assertTrue(MathUtil.isApproximatelyEqual(d1, d2, TOLERANCE));
    }

}
