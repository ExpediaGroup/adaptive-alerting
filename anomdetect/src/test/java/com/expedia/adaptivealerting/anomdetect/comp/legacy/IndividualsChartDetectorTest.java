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
package com.expedia.adaptivealerting.anomdetect.comp.legacy;

import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyType;
import com.expedia.adaptivealerting.core.util.MathUtil;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.opencsv.bean.CsvToBeanBuilder;
import junit.framework.TestCase;
import lombok.val;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;

public class IndividualsChartDetectorTest {
    private static final int WARMUP_PERIOD = 25;

    // TODO This tolerance is very loose. Can we tighten it up? [WLW]
    private static final double TOLERANCE = 0.1;

    private UUID detectorUUID;
    private MetricDefinition metricDefinition;
    private long epochSecond;
    private static List<IndividualsChartTestRow> data;


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
        val testRows = data.listIterator();
        val testRow0 = testRows.next();
        val observed0 = testRow0.getObserved();

        val params = new IndividualsControlChartParams()
                .setInitValue(observed0)
                .setWarmUpPeriod(WARMUP_PERIOD);
        val detector = new IndividualsControlChartDetector();
        detector.init(detectorUUID, params, AnomalyType.TWO_TAILED);

        int noOfDataPoints = 1;

        while (testRows.hasNext()) {
            final IndividualsChartTestRow testRow = testRows.next();
            final double observed = testRow.getObserved();

            final MetricData metricData = new MetricData(metricDefinition, observed, epochSecond);
            final AnomalyLevel level = detector.classify(metricData).getAnomalyLevel();

            if (noOfDataPoints < WARMUP_PERIOD) {
                assertEquals(AnomalyLevel.MODEL_WARMUP, level);
            } else {
                assertApproxEqual(testRow.getUpperControlLimit_R(), detector.getUpperControlLimit_R());
                assertApproxEqual(testRow.getLowerControlLimit_X(), detector.getLowerControlLimit_X());
                assertApproxEqual(testRow.getUpperControlLimit_X(), detector.getUpperControlLimit_X());
                assertEquals(AnomalyLevel.valueOf(testRow.getAnomalyLevel()), level);
            }
            noOfDataPoints += 1;
        }
    }

    private static void assertApproxEqual(double d1, double d2) {
        TestCase.assertTrue(MathUtil.isApproximatelyEqual(d1, d2, TOLERANCE));
    }

    private static void readDataFromCsv() {
        final InputStream is = ClassLoader.getSystemResourceAsStream("tests/individual-chart-sample-input.csv");
        data = new CsvToBeanBuilder<IndividualsChartTestRow>(new InputStreamReader(is))
                .withType(IndividualsChartTestRow.class)
                .build()
                .parse();
    }
}
