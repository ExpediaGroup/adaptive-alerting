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
package com.expedia.adaptivealerting.anomdetect.forecast.interval;

import com.expedia.adaptivealerting.anomdetect.forecast.interval.config.ExponentialWelfordIntervalForecasterParams;
import com.expedia.adaptivealerting.anomdetect.util.TestObjectMother;
import com.expedia.metrics.MetricData;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.Data;
import lombok.val;
import lombok.var;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStreamReader;
import java.time.Instant;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ExponentialWelfordIntervalForecasterTest {
    private static final String TEST_DATA_FILE = "tests/exp-welford-test-data.csv";
    private static final double TOLERANCE = 0.001;

    private static List<TestRow> testData;

    private ExponentialWelfordIntervalForecaster forecasterUnderTest;

    @BeforeClass
    public static void setUpClass() {
        testData = readTestData();
    }

    @Before
    public void setUp() {
        val params = new ExponentialWelfordIntervalForecasterParams()
                .setAlpha(0.15)
                .setInitVarianceEstimate(1.0)
                .setWeakSigmas(3.0)
                .setStrongSigmas(4.0);
        params.validate();
        this.forecasterUnderTest = new ExponentialWelfordIntervalForecaster(params);
    }

    @Test
    public void coverageOnly() {
        forecasterUnderTest.getParams();
    }

    @Test
    public void testForecast() {
        val metricDef = TestObjectMother.metricDefinition();
        val secondBase = Instant.now().getEpochSecond();
        var secondOffset = 0;

        for (val testRow : testData) {
            val metricData = new MetricData(metricDef, testRow.getObservation(), secondBase + secondOffset);
            val actual = forecasterUnderTest.forecast(metricData, testRow.getPointForecast());

            assertEquals(testRow.getUpperStrong(), actual.getUpperStrong(), TOLERANCE);
            assertEquals(testRow.getUpperWeak(), actual.getUpperWeak(), TOLERANCE);
            assertEquals(testRow.getLowerWeak(), actual.getLowerWeak(), TOLERANCE);
            assertEquals(testRow.getLowerStrong(), actual.getLowerStrong(), TOLERANCE);
        }
    }

    private static List<TestRow> readTestData() {
        val is = ClassLoader.getSystemResourceAsStream(TEST_DATA_FILE);
        return new CsvToBeanBuilder<TestRow>(new InputStreamReader(is))
                .withType(TestRow.class)
                .build()
                .parse();
    }

    @Data
    public static class TestRow {

        @CsvBindByName(column = "observation")
        private double observation;

        @CsvBindByName(column = "point_forecast")
        private double pointForecast;

        @CsvBindByName(column = "up_strong")
        private double upperStrong;

        @CsvBindByName(column = "up_weak")
        private double upperWeak;

        @CsvBindByName(column = "lo_weak")
        private double lowerWeak;

        @CsvBindByName(column = "lo_strong")
        private double lowerStrong;
    }
}
