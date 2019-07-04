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
package com.expedia.adaptivealerting.anomdetect.forecast.point.algo;

import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.val;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStreamReader;
import java.time.Instant;
import java.util.List;

import static org.junit.Assert.assertEquals;

public final class EwmaPointForecasterTest {
    private static final double TOLERANCE = 0.001;

    private MetricDefinition metricDef;
    private long epochSecond;
    private static List<EwmaPointForecasterTestRow> data;

    @BeforeClass
    public static void setUpClass() {
        readData_calInflow();
    }

    @Before
    public void setUp() {
        this.metricDef = new MetricDefinition("some-key");
        this.epochSecond = Instant.now().getEpochSecond();
    }

    @Test
    public void classify() {
        val testRows = data.listIterator();
        val testRow0 = testRows.next();
        val observed0 = testRow0.getObserved();

        val params = new EwmaPointForecasterParams()
                .setAlpha(0.05)
                .setInitMeanEstimate(observed0);
        val forecaster = new EwmaPointForecaster(params);

        assertEquals(params, forecaster.getParams());
        assertEquals(observed0, forecaster.getMean(), TOLERANCE);

        while (testRows.hasNext()) {
            val testRow = testRows.next();
            val observed = testRow.getObserved();
            val metricData = new MetricData(metricDef, observed, epochSecond);
            forecaster.forecast(metricData);
            assertEquals(testRow.getKnownMean(), forecaster.getMean(), TOLERANCE);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForecast_nullMetricData() {
        val forecaster = new EwmaPointForecaster();
        forecaster.forecast(null);
    }

    private static void readData_calInflow() {
        val is = ClassLoader.getSystemResourceAsStream("tests/cal-inflow-tests-ewma.csv");
        data = new CsvToBeanBuilder<EwmaPointForecasterTestRow>(new InputStreamReader(is))
                .withType(EwmaPointForecasterTestRow.class)
                .build()
                .parse();
    }
}
