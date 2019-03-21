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
package com.expedia.adaptivealerting.anomdetect.forecast.point;

import com.expedia.adaptivealerting.core.util.MathUtil;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.opencsv.bean.CsvToBeanBuilder;
import junit.framework.TestCase;
import lombok.val;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStreamReader;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EwmaDetectorTest {
    private static final double TOLERANCE = 0.001;

    private MetricDefinition metricDefinition;
    private long epochSecond;
    private static List<EwmaTestRow> data;

    @BeforeClass
    public static void setUpClass() {
        readData_calInflow();
    }

    @Before
    public void setUp() {
        this.metricDefinition = new MetricDefinition("some-key");
        this.epochSecond = Instant.now().getEpochSecond();
    }

    @Test
    public void testInit() {
        val detector = new EwmaDetector();
        detector.init(UUID.randomUUID(), new EwmaParams());
        assertNotNull(detector.getUuid());
        assertNotNull(detector.getParams());
        assertEquals(EwmaParams.class, detector.getParamsClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInit_alphaOutOfRange() {
        new EwmaDetector().init(UUID.randomUUID(), new EwmaParams().setAlpha(2.0));
    }

    @Test
    public void classify() {
        val testRows = data.listIterator();
        val testRow0 = testRows.next();
        val observed0 = testRow0.getObserved();

        val params = new EwmaParams()
                .setAlpha(0.05)
                .setInitMeanEstimate(observed0);
        val detector = new EwmaDetector();
        detector.init(UUID.randomUUID(), params);

        assertEquals(observed0, detector.getMean());
        assertEquals(0.0, detector.getVariance());

        while (testRows.hasNext()) {
            val testRow = testRows.next();
            val observed = testRow.getObserved();

            val metricData = new MetricData(metricDefinition, observed, epochSecond);
            detector.classify(metricData);

            // TODO: Move this to GenerateCalInflowTestsEwma.R
            assertApproxEqual(testRow.getKnownMean(), testRow.getMean());

            assertApproxEqual(testRow.getMean(), detector.getMean());
            assertApproxEqual(testRow.getVar(), detector.getVariance());
            // TODO: Assert AnomalyResult.getAnomalyLevel matches expected
        }
    }

    private static void readData_calInflow() {
        val is = ClassLoader.getSystemResourceAsStream("tests/cal-inflow-tests-ewma.csv");
        data = new CsvToBeanBuilder<EwmaTestRow>(new InputStreamReader(is))
                .withType(EwmaTestRow.class)
                .build()
                .parse();
    }

    private static void assertApproxEqual(double d1, double d2) {
        // TODO: This could use Assert.assertEquals(double expected, double actual, double delta)
        TestCase.assertTrue(MathUtil.isApproximatelyEqual(d1, d2, TOLERANCE));
    }
}
