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
package com.expedia.adaptivealerting.anomdetect.algo;

import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.lang.Math.sqrt;
import static junit.framework.TestCase.assertEquals;

@Slf4j
public final class PewmaAnomalyDetectorTest {
    private static final double WEAK_SIGMAS = 2.0;
    private static final double STRONG_SIGMAS = 3.0;
    private static final double DEFAULT_ALPHA = 0.05;
    private static final double TOLERANCE = 0.00001;

    private static final String SAMPLE_INPUT_PATH = "tests/pewma-sample-input.csv";
    private static final String CAL_INFLOW_PATH = "tests/cal-inflow-tests-pewma.csv";

    private UUID detectorUuid;
    private MetricDefinition metricDefinition;
    private long epochSecond;

    @Before
    public void setUp() {
        this.detectorUuid = UUID.randomUUID();
        this.metricDefinition = new MetricDefinition("some-key");
        this.epochSecond = Instant.now().getEpochSecond();
    }

    @Test
    public void pewmaCloseToEwmaWithZeroBeta() throws IOException {
        val beta = 0.0;

        val testRows = readData_sampleInput().listIterator();
        val observed0 = Double.parseDouble(testRows.next()[0]);

        val pewmaParams = new PewmaParams()
                .setAlpha(DEFAULT_ALPHA)
                .setBeta(beta)
                .setWeakSigmas(WEAK_SIGMAS)
                .setStrongSigmas(STRONG_SIGMAS)
                .setInitMeanEstimate(observed0);
        val pewmaDetector = new PewmaDetector();
        pewmaDetector.init(detectorUuid, pewmaParams);

        val ewmaParams = new EwmaParams()
                .setAlpha(DEFAULT_ALPHA)
                .setWeakSigmas(WEAK_SIGMAS)
                .setStrongSigmas(STRONG_SIGMAS)
                .setInitMeanEstimate(observed0);
        val ewmaDetector = new EwmaDetector();
        ewmaDetector.init(UUID.randomUUID(), ewmaParams);

        int rowCount = 1;
        while (testRows.hasNext()) {
            val observed = Float.parseFloat(testRows.next()[0]);

            val ewmaStdDev = sqrt(ewmaDetector.getVariance());

            val threshold = 1.0 / rowCount; // results converge with more iterations
            assertEquals(ewmaDetector.getMean(), pewmaDetector.getMean(), threshold);
            assertEquals(ewmaStdDev, pewmaDetector.getStdDev(), threshold);

            val metricData = new MetricData(metricDefinition, observed, epochSecond);
            val pewmaLevel = pewmaDetector.classify(metricData).getAnomalyLevel();
            val ewmaLevel = ewmaDetector.classify(metricData).getAnomalyLevel();

            if (rowCount > pewmaParams.getWarmUpPeriod()) {
                assertEquals(pewmaLevel, ewmaLevel);
            }
            rowCount++;
        }
    }

    @Test
    public void evaluate() {
        val testRows = readData_calInflow().listIterator();
        val observed0 = testRows.next().getObserved();

        val params = new PewmaParams()
                .setAlpha(DEFAULT_ALPHA)
                .setBeta(0.5)
                .setWeakSigmas(WEAK_SIGMAS)
                .setStrongSigmas(STRONG_SIGMAS)
                .setInitMeanEstimate(observed0);
        val detector = new PewmaDetector();
        detector.init(detectorUuid, params);

        while (testRows.hasNext()) {
            val testRow = testRows.next();
            val observed = testRow.getObserved();
            val metricData = new MetricData(metricDefinition, observed, epochSecond);
            val level = detector.classify(metricData).getAnomalyLevel();

            assertEquals(testRow.getMean(), detector.getMean(), TOLERANCE);
            assertEquals(testRow.getStd(), detector.getStdDev(), TOLERANCE);
            assertEquals(AnomalyLevel.valueOf(testRow.getLevel()), level);
        }
    }

    private static List<String[]> readData_sampleInput() throws IOException {
        val is = ClassLoader.getSystemResourceAsStream(SAMPLE_INPUT_PATH);
        val reader = new CSVReader(new InputStreamReader(is));
        return reader.readAll();
    }

    private static List<PewmaTestRow> readData_calInflow() {
        val is = ClassLoader.getSystemResourceAsStream(CAL_INFLOW_PATH);
        return new CsvToBeanBuilder<PewmaTestRow>(new InputStreamReader(is))
                .withType(PewmaTestRow.class)
                .build()
                .parse();
    }
}

