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
    private static final int AUSTOURISTS_PERIOD = 4;
    public static final double AUSTOURISTS_ALPHA = 0.441;
    public static final double AUSTOURISTS_BETA = 0.030;
    public static final double AUSTOURISTS_GAMMA = 0.002;

    private UUID detectorUUID;
    private MetricDefinition metricDefinition;
    private long epochSecond;
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
        Assert.assertArrayEquals(expectedReverseSeasonals, actualReverseHistorySeasonals, TOLERANCE);
        // TODO: Record expected anomaly level in R test gen code
        // Assert.assertEquals(testRow.getExpectedLevel(), result.getAnomalyLevel());
        Assert.assertEquals(testRow.getYHat(), forecastBeforeObservation, TOLERANCE);
    }

}
