/*
 * Copyright 2019 Expedia Group, Inc.
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
package com.expedia.adaptivealerting.anomdetect.holtwinters;

import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.opencsv.bean.CsvToBeanBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

/**
 * @author Matt Callanan
 */
public class HoltWintersAnomalyDetectorTest {
    private static final double TOLERANCE = 0.001;

    private UUID detectorUUID;
    private MetricDefinition metricDefinition;
    private long epochSecond;
    private static List<HoltWintersTestRow> ADDITIVE_TEST_DATA =  readData("tests/ads-tests-holtwinters-additive.csv");
    private static List<HoltWintersTestRow> MULTIPLICATIVE_TEST_DATA = readData("tests/ads-tests-holtwinters-multiplicative.csv");

    @Before
    public void setUp() {
        this.detectorUUID = UUID.randomUUID();
        this.metricDefinition = new MetricDefinition("some-key");
        this.epochSecond = Instant.now().getEpochSecond();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_alphaOutOfRange() {
        final HoltWintersParams params = new HoltWintersParams()
                .setPeriod(24)
                .setAlpha(2.0);
        new HoltWintersAnomalyDetector(detectorUUID, params);
    }

    // TODO HW: Use austourists instead of ads. Generate expected values from R fpp2 package and test against that
    @Test
    public void testAdditive() {
        final ListIterator<HoltWintersTestRow> testRows = ADDITIVE_TEST_DATA.listIterator();

        int PERIOD = 24;
        // These values were copied from the first time the model was run
        double[] initSeasonal = {-43253.33333,
                                 -43483.33333,
                                 -34043.33333,
                                 -21438.33333,
                                 -1738.333333,
                                 -6893.333333,
                                 -16873.33333,
                                 -20573.33333,
                                 -15313.33333,
                                 -7243.333333,
                                 7661.666667,
                                 25651.66667,
                                 34221.66667,
                                 27346.66667,
                                 25926.66667,
                                 26731.66667,
                                 21411.66667,
                                 27321.66667,
                                 40471.66667,
                                 42866.66667,
                                 16151.66667,
                                 -17473.33333,
                                 -26588.33333,
                                 -40848.3333
         };
        final HoltWintersParams params = new HoltWintersParams()
                .setPeriod(PERIOD)
                .setAlpha(0.05)
                .setSeasonalityType(SeasonalityType.ADDITIVE)
                .setInitLevelEstimate(164500)
                .setInitBaseEstimate(4200)
                .setInitSeasonalEstimates(initSeasonal)
                .setInitForecastEstimate(80000);
        final HoltWintersAnomalyDetector detector = new HoltWintersAnomalyDetector(detectorUUID, params);

        while (testRows.hasNext()) {
            final HoltWintersTestRow testRow = testRows.next();
            final int observed = testRow.getObserved();
            final MetricData metricData = new MetricData(metricDefinition, observed, epochSecond);

            AnomalyResult result = detector.classify(metricData);

            BigDecimal roundedForecast = roundDecimalTwoPlaces(detector.getComponents().getForecast());
            BigDecimal roundedMean = roundDecimalTwoPlaces(detector.getComponents().getMean());
            Assert.assertEquals(testRow.getExpectedMean(), roundedMean.doubleValue(), TOLERANCE);
            Assert.assertEquals(testRow.getExpectedLevel(), result.getAnomalyLevel());
            Assert.assertEquals(testRow.getExpectedForecast(), roundedForecast.doubleValue(), TOLERANCE);
        }
    }

    // TODO HW: Use austourists instead of ads. Generate expected values from R fpp2 package and test against that
    @Test
    public void testMultiplicative() {
        final ListIterator<HoltWintersTestRow> testRows = MULTIPLICATIVE_TEST_DATA.listIterator();

        int PERIOD = 24;
        // These values were copied from the initial 24 values generated the first time the model was run
        double[] initSeasonal = {0.649396793,
                                 0.647532457,
                                 0.724051283,
                                 0.82622499,
                                 0.985909404,
                                 0.944123965,
                                 0.863228003,
                                 0.833236514,
                                 0.875873063,
                                 0.941286932,
                                 1.062103998,
                                 1.20792748,
                                 1.277394253,
                                 1.221666824,
                                 1.210156577,
                                 1.216681752,
                                 1.173558855,
                                 1.221464179,
                                 1.328055552,
                                 1.347468962,
                                 1.130922306,
                                 0.858364518,
                                 0.78448008,
                                 0.66889126
         };
        final HoltWintersParams params = new HoltWintersParams()
                .setPeriod(PERIOD)
                .setAlpha(0.05)
                .setSeasonalityType(SeasonalityType.MULTIPLICATIVE)
                .setInitLevelEstimate(164500)
                .setInitBaseEstimate(4200)
                .setInitSeasonalEstimates(initSeasonal)
                .setInitForecastEstimate(80000);
        final HoltWintersAnomalyDetector detector = new HoltWintersAnomalyDetector(detectorUUID, params);

        while (testRows.hasNext()) {
            final HoltWintersTestRow testRow = testRows.next();
            final int observed = testRow.getObserved();
            final MetricData metricData = new MetricData(metricDefinition, observed, epochSecond);

            AnomalyResult result = detector.classify(metricData);

            BigDecimal roundedMean = roundDecimalTwoPlaces(detector.getComponents().getMean());
            BigDecimal roundedForecast = roundDecimalTwoPlaces(detector.getComponents().getForecast());
            Assert.assertEquals(testRow.getExpectedMean(), roundedMean.doubleValue(), TOLERANCE);
            Assert.assertEquals(testRow.getExpectedLevel(), result.getAnomalyLevel());
            Assert.assertEquals(testRow.getExpectedForecast(), roundedForecast.doubleValue(), TOLERANCE);
        }
    }

    private BigDecimal roundDecimalTwoPlaces(double overallMean) {
        return new BigDecimal(String.valueOf(overallMean)).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private static List<HoltWintersTestRow> readData(String filename) {
        final InputStream is = ClassLoader.getSystemResourceAsStream(filename);
        return new CsvToBeanBuilder<HoltWintersTestRow>(new InputStreamReader(is))
                .withType(HoltWintersTestRow.class)
                .build()
                .parse();
    }
}
