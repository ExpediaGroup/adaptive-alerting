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

import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.time.Instant;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import static com.expedia.adaptivealerting.anomdetect.holtwinters.SeasonalityType.ADDITIVE;
import static com.expedia.adaptivealerting.anomdetect.holtwinters.SeasonalityType.MULTIPLICATIVE;
import static com.expedia.adaptivealerting.anomdetect.util.CsvToBeanFileReader.readData;

/**
 * @author Matt Callanan
 */
public class HoltWintersAnomalyDetectorTest {
    private static final double TOLERANCE = 0.001;
    // private static final int ADS_PERIOD = 24;
    private static final int AUSTOURISTS_PERIOD = 4;
    public static final double AUSTOURISTS_ALPHA = 0.441;
    public static final double AUSTOURISTS_BETA = 0.030;
    public static final double AUSTOURISTS_GAMMA = 0.002;

    private UUID detectorUUID;
    private MetricDefinition metricDefinition;
    private long epochSecond;
    // private static List<HoltWintersTestRowAds> ADS_ADDITIVE_TEST_DATA = readData("tests/ads-tests-holtwinters-additive.csv", HoltWintersTestRowAds.class);
    // private static List<HoltWintersTestRowAds> ADS_MULTIPLICATIVE_TEST_DATA = readData("tests/ads-tests-holtwinters-multiplicative.csv", HoltWintersTestRowAds.class);
    public static final String AUSTOURISTS_ADD_FILE = "tests/austourists-tests-holtwinters-additive.csv";
    public static final String AUSTOURISTS_MULT_FILE = "tests/austourists-tests-holtwinters-multiplicative.csv";
    private static List<HoltWintersTestRowAustourists> AUSTOURISTS_ADD_DATA = readData(AUSTOURISTS_ADD_FILE, HoltWintersTestRowAustourists.class);
    private static List<HoltWintersTestRowAustourists> AUSTOURISTS_MULT_DATA = readData(AUSTOURISTS_MULT_FILE, HoltWintersTestRowAustourists.class);

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

    @Test
    public void testAdditiveAustouristsProvidingInitialEstimates() {
        doAustouristsTest(AUSTOURISTS_ADD_DATA, ADDITIVE);
    }

    @Test
    public void testMultiplicativeAustouristsProvidingInitialEstimates() {
        doAustouristsTest(AUSTOURISTS_MULT_DATA, MULTIPLICATIVE);
    }

    @Test
    @Ignore
    public void testAdditiveAustouristsLearningInitialEstimates() {
        // TODO HW: Implement learning functionality and tests
    }

    @Test
    @Ignore
    public void testMultiplicativeAustouristsLearningInitialEstimates() {
        // TODO HW: Implement learning functionality and tests
    }

    /**
     * Tests Holt-Winters functionality by comparing with data generated from Hyndman's R "fpp2" library - see GenerateAustouristsTests.R
     */
    private void doAustouristsTest(List<HoltWintersTestRowAustourists> testData, SeasonalityType seasonalityType) {
        final ListIterator<HoltWintersTestRowAustourists> testRows = testData.listIterator();
        final HoltWintersTestRowAustourists firstRow = testRows.next();
        final HoltWintersParams params = buildParams(seasonalityType, firstRow);
        final HoltWintersAnomalyDetector subject = new HoltWintersAnomalyDetector(detectorUUID, params);

        while (testRows.hasNext()) {
            final HoltWintersTestRowAustourists testRow = testRows.next();
            final double forecastBeforeObservation = subject.getComponents().getForecast();
            subject.classify(new MetricData(metricDefinition, testRow.getY(), epochSecond));
            checkValues(testRow, forecastBeforeObservation, subject.getComponents());
        }
    }

    private HoltWintersParams buildParams(SeasonalityType seasonalityType, HoltWintersTestRowAustourists testRow) {
        return new HoltWintersParams()
                    .setPeriod(AUSTOURISTS_PERIOD)
                    .setAlpha(AUSTOURISTS_ALPHA)
                    .setBeta(AUSTOURISTS_BETA)
                    .setGamma(AUSTOURISTS_GAMMA)
                    .setSeasonalityType(seasonalityType)
                    .setWarmUpPeriod(AUSTOURISTS_PERIOD)
                    .setInitLevelEstimate(testRow.getL())
                    .setInitBaseEstimate(testRow.getB())
                    .setInitSeasonalEstimates(new double[]{testRow.getS4(), testRow.getS3(), testRow.getS2(), testRow.getS1()});
    }

    private void checkValues(HoltWintersTestRowAustourists testRow, double forecastBeforeObservation, HoltWintersOnlineComponents components) {
        Assert.assertEquals(testRow.getL(), components.getLevel(), TOLERANCE);
        Assert.assertEquals(testRow.getB(), components.getBase(), TOLERANCE);
        double[] expectedReverseSeasonals = {testRow.getS1(), testRow.getS2(), testRow.getS3(), testRow.getS4()};
        double[] actualReverseHistorySeasonals = components.getReverseHistorySeasonals();
//            System.out.println("expectedReverseSeasonals      = " + Arrays.toString(expectedReverseSeasonals));
//            System.out.println("actualReverseHistorySeasonals = " + Arrays.toString(actualReverseHistorySeasonals));
        Assert.assertArrayEquals(expectedReverseSeasonals, actualReverseHistorySeasonals, TOLERANCE);
        // TODO: Record expected anomaly level in R test gen code
        // Assert.assertEquals(testRow.getExpectedLevel(), result.getAnomalyLevel());
        Assert.assertEquals(testRow.getYHat(), forecastBeforeObservation, TOLERANCE);
    }


//    @Test
//    public void testAdditiveAds() {
//        final ListIterator<HoltWintersTestRowAds> testRows = ADS_ADDITIVE_TEST_DATA.listIterator();
//        // These values were copied from the initial 24 values generated the first time the model was run
//        double[] initSeasonal = {-43253.33333, -43483.33333, -34043.33333, -21438.33333, -1738.333333, -6893.333333, -16873.33333, -20573.33333, -15313.33333, -7243.333333, 7661.666667, 25651.66667, 34221.66667, 27346.66667, 25926.66667, 26731.66667, 21411.66667, 27321.66667, 40471.66667, 42866.66667, 16151.66667, -17473.33333, -26588.33333, -40848.3333};
//        testAdsClassification(testRows, ADS_PERIOD, initSeasonal, SeasonalityType.ADDITIVE);
//    }

//    @Test
//    public void testMultiplicativeAds() {
//        final ListIterator<HoltWintersTestRowAds> testRows = ADS_MULTIPLICATIVE_TEST_DATA.listIterator();
//        // These values were copied from the initial 24 values generated the first time the model was run
//        double[] initSeasonal = {0.649396793, 0.647532457, 0.724051283, 0.82622499, 0.985909404, 0.944123965, 0.863228003, 0.833236514, 0.875873063, 0.941286932, 1.062103998, 1.20792748, 1.277394253, 1.221666824, 1.210156577, 1.216681752, 1.173558855, 1.221464179, 1.328055552, 1.347468962, 1.130922306, 0.858364518, 0.78448008, 0.66889126};
//        testAdsClassification(testRows, ADS_PERIOD, initSeasonal, SeasonalityType.MULTIPLICATIVE);
//    }

//    private void testAdsClassification(ListIterator<HoltWintersTestRowAds> testRows, int PERIOD, double[] initSeasonal, SeasonalityType seasonalityType) {
//        final HoltWintersParams params = new HoltWintersParams()
//                .setPeriod(PERIOD)
//                .setAlpha(0.05)
//                .setSeasonalityType(seasonalityType)
//                .setInitLevelEstimate(164500)
//                .setInitBaseEstimate(4200)
//                .setInitSeasonalEstimates(initSeasonal)
//                .setInitForecastEstimate(80000);
//        final HoltWintersAnomalyDetector detector = new HoltWintersAnomalyDetector(detectorUUID, params);
//
//        System.out.print("Forecast, expectedForecast, ");
//        boolean firstRow = true;
//        while (testRows.hasNext()) {
//            final HoltWintersTestRowAds testRow = testRows.next();
//            final int observed = testRow.getObserved();
//            final MetricData metricData = new MetricData(metricDefinition, observed, epochSecond);
//
//            System.out.println("testRow = " + testRow);
////            BigDecimal roundedForecast = roundDecimalTwoPlaces(detector.getComponents().getForecast());
//            double forecast = detector.getComponents().getForecast();
//            System.out.println("forecast = " + forecast);
//            Assert.assertEquals(testRow.getExpectedForecast(), forecast, TOLERANCE);
//            System.out.print(String.format("%f, %f, ", forecast, testRow.getExpectedForecast()));
//            AnomalyResult result = detector.classify(metricData, true);
//
//            BigDecimal roundedMean = roundDecimalTwoPlaces(detector.getComponents().getMean());
//            Assert.assertEquals(testRow.getExpectedMean(), roundedMean.doubleValue(), TOLERANCE);
//            Assert.assertEquals(testRow.getExpectedLevel(), result.getAnomalyLevel());
//            firstRow = false;
//        }
//    }

//    private BigDecimal roundDecimalTwoPlaces(double overallMean) {
//        return new BigDecimal(String.valueOf(overallMean)).setScale(2, BigDecimal.ROUND_HALF_UP);
//    }

}
