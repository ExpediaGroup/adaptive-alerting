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
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.time.Instant;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import static com.expedia.adaptivealerting.anomdetect.holtwinters.HoltWintersAustouristsTestHelper.AUSTOURISTS_ADD_DATA;
import static com.expedia.adaptivealerting.anomdetect.holtwinters.HoltWintersAustouristsTestHelper.AUSTOURISTS_MULT_DATA;
import static com.expedia.adaptivealerting.anomdetect.holtwinters.HoltWintersAustouristsTestHelper.buildAustouristsParams;
import static com.expedia.adaptivealerting.anomdetect.holtwinters.SeasonalityType.ADDITIVE;
import static com.expedia.adaptivealerting.anomdetect.holtwinters.SeasonalityType.MULTIPLICATIVE;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Matt Callanan
 */
public class HoltWintersAnomalyDetectorTest {
    private static final double TOLERANCE = 0.001;

    private UUID detectorUUID;
    private MetricDefinition metricDefinition;
    private long epochSecond;

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
    private void doAustouristsTest(List<HoltWintersAustouristsTestRow> testData, SeasonalityType seasonalityType) {
        final ListIterator<HoltWintersAustouristsTestRow> testRows = testData.listIterator();
        final HoltWintersAustouristsTestRow firstRow = testRows.next();
        double initLevelEstimate = firstRow.getL();
        double initBaseEstimate = firstRow.getB();
        double[] initSeasonalEstimates = {firstRow.getS4(), firstRow.getS3(), firstRow.getS2(), firstRow.getS1()};
        final HoltWintersParams params = buildAustouristsParams(seasonalityType, initLevelEstimate, initBaseEstimate, initSeasonalEstimates);
        final HoltWintersAnomalyDetector subject = new HoltWintersAnomalyDetector(detectorUUID, params);

        while (testRows.hasNext()) {
            final HoltWintersAustouristsTestRow testRow = testRows.next();
            final double forecastBeforeObservation = subject.getComponents().getForecast();
            subject.classify(new MetricData(metricDefinition, testRow.getY(), epochSecond));
            checkValues(testRow, forecastBeforeObservation, subject.getComponents());
        }
    }

    private void checkValues(HoltWintersAustouristsTestRow testRow, double forecastBeforeObservation, HoltWintersOnlineComponents components) {
        assertEquals(testRow.getL(), components.getLevel(), TOLERANCE);
        assertEquals(testRow.getB(), components.getBase(), TOLERANCE);
        double[] expectedReverseSeasonals = {testRow.getS1(), testRow.getS2(), testRow.getS3(), testRow.getS4()};
        double[] actualReverseHistorySeasonals = components.getReverseHistorySeasonals();
        assertArrayEquals(expectedReverseSeasonals, actualReverseHistorySeasonals, TOLERANCE);
        // TODO HW: Record expected anomaly level in R test gen code
        // Assert.assertEquals(testRow.getExpectedLevel(), result.getAnomalyLevel());
        assertEquals(testRow.getYHat(), forecastBeforeObservation, TOLERANCE);
    }

}
