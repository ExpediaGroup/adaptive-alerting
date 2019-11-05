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
package com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.individuals;

import com.expedia.adaptivealerting.anomdetect.detect.AnomalyLevel;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.OutlierDetectorResult;
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
import static org.junit.Assert.assertSame;

public class IndividualsDetectorTest {
    private static final int WARMUP_PERIOD = 25;

    // TODO This tolerance is a bit loose. Can we tighten it up? [WLW]
    private static final double TOLERANCE = 0.01;

    private UUID detectorUuid;
    private MetricDefinition metricDef;
    private long epochSecond;
    private static List<IndividualsDetectorTestRow> data;
    private boolean trusted;


    @BeforeClass
    public static void setUpClass() {
        readDataFromCsv();
    }

    @Before
    public void setUp() {
        this.detectorUuid = UUID.randomUUID();
        this.metricDef = new MetricDefinition("some-key");
        this.epochSecond = Instant.now().getEpochSecond();
        this.trusted = true;
    }

    @Test
    public void testEvaluate() {
        val testRows = data.listIterator();
        val testRow0 = testRows.next();
        val observed0 = testRow0.getObserved();

        val params = new IndividualsDetectorParams()
                .setInitValue(observed0)
                .setWarmUpPeriod(WARMUP_PERIOD);
        val detector = new IndividualsDetector(detectorUuid, params, trusted);

        assertEquals(detectorUuid, detector.getUuid());
        assertEquals(trusted, detector.isTrusted());
        assertSame(params, detector.getParams());

        int noOfDataPoints = 1;

        while (testRows.hasNext()) {
            val testRow = testRows.next();
            val observed = testRow.getObserved();

            val metricData = new MetricData(metricDef, observed, epochSecond);
            val result = (OutlierDetectorResult) detector.detect(metricData);
            val level = result.getAnomalyLevel();

            if (noOfDataPoints < WARMUP_PERIOD) {
                TestCase.assertEquals(AnomalyLevel.MODEL_WARMUP, level);
            } else {
                assertEquals(testRow.getUpperControlLimit_R(), detector.getUpperControlLimit_R(), TOLERANCE);
                assertEquals(testRow.getLowerControlLimit_X(), detector.getLowerControlLimit_X(), TOLERANCE);
                assertEquals(testRow.getUpperControlLimit_X(), detector.getUpperControlLimit_X(), TOLERANCE);
                assertEquals(AnomalyLevel.valueOf(testRow.getAnomalyLevel()), level);
            }
            noOfDataPoints += 1;
        }
    }

    private static void readDataFromCsv() {
        val is = ClassLoader.getSystemResourceAsStream("tests/individual-chart-sample-input.csv");
        data = new CsvToBeanBuilder<IndividualsDetectorTestRow>(new InputStreamReader(is))
                .withType(IndividualsDetectorTestRow.class)
                .build()
                .parse();
    }
}
