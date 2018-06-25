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
    private static final double TOLERANCE = 0.1;

    private static List<CusumTestRow> data;

    @BeforeClass
    public static void setUpClass() throws IOException {
        readData_calInflow();
    }

    @Test
    public void testDefaultConstructor() {
        final CusumAnomalyDetector detector = new CusumAnomalyDetector();
        assertEquals(0.15, detector.getAlpha());
        assertEquals(WEAK_SIGMAS, detector.getWeakThresholdSigmas());
        assertEquals(STRONG_SIGMAS, detector.getStrongThresholdSigmas());
    }

    @Test
    public void testEvaluate() {

        // Params
        final double alpha = 0.05;

        final ListIterator<CusumTestRow> testRows = data.listIterator();
        final CusumTestRow testRow0 = testRows.next();
        final double observed0 = testRow0.getObserved();
        final CusumAnomalyDetector detector = new CusumAnomalyDetector(0, alpha, observed0, WEAK_SIGMAS, STRONG_SIGMAS,
                0.16);

        // Params
        assertEquals(alpha, detector.getAlpha());
        assertEquals(WEAK_SIGMAS, detector.getWeakThresholdSigmas());
        assertEquals(STRONG_SIGMAS, detector.getStrongThresholdSigmas());

        // Seed observation
        assertEquals(observed0, detector.getMean());
        assertEquals(0.0, detector.getVariance());

        while (testRows.hasNext()) {
            final CusumTestRow testRow = testRows.next();
            final double observed = testRow.getObserved();
            detector.classify(MetricPointUtil.metricPoint(Instant.now(), observed));
            assertApproxEqual(testRow.getSh(), detector.getHighCusum());
            assertApproxEqual(testRow.getSl(), detector.getLowCusum());
            assertApproxEqual(testRow.getStdDev(), Math.sqrt(detector.getVariance()));
        }
    }

    private static void readData_calInflow() {
        final InputStream is = ClassLoader.getSystemResourceAsStream("tests/cusum-sample-input.csv");
        data = new CsvToBeanBuilder<CusumTestRow>(new InputStreamReader(is)).withType(CusumTestRow.class).build()
                .parse();

    }

    private static void assertApproxEqual(double d1, double d2) {
        TestCase.assertTrue(MathUtil.isApproximatelyEqual(d1, d2, TOLERANCE));
    }

}
