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
import lombok.val;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Instant;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import static com.expedia.adaptivealerting.anomdetect.holtwinters.HoltWintersAustouristsTestHelper.*;
import static com.expedia.adaptivealerting.anomdetect.holtwinters.HoltWintersTrainingMethod.SIMPLE;
import static com.expedia.adaptivealerting.anomdetect.holtwinters.SeasonalityType.ADDITIVE;
import static com.expedia.adaptivealerting.anomdetect.holtwinters.SeasonalityType.MULTIPLICATIVE;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Tests Holt-Winters functionality by comparing with data generated from Hyndman's R "fpp2" library - see GenerateAustouristsTests.R
 */
public class HoltWintersAnomalyDetectorTest {
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

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
    public void testInit_alphaOutOfRange() {
        val params = new HoltWintersParams()
                .setFrequency(24)
                .setAlpha(2.0);
        val detector = new HoltWintersAnomalyDetector();
        detector.init(detectorUUID, params);
    }

    @Test
    public void testAdditiveProvidingInitialEstimates() {
        doAustouristsTest(AUSTOURISTS_ADD_DATA, ADDITIVE, false);
    }

    @Test
    public void testMultiplicativeProvidingInitialEstimates() {
        doAustouristsTest(AUSTOURISTS_MULT_DATA, MULTIPLICATIVE, false);
    }

    @Test
    public void testAdditiveWithTraining() {
        doAustouristsTest(AUSTOURISTS_ADD_DATA, ADDITIVE, true);
    }

    @Test
    public void testMultiplicativeWithTraining() {
        doAustouristsTest(AUSTOURISTS_MULT_DATA, MULTIPLICATIVE, true);
    }

    private void doAustouristsTest(List<HoltWintersAustouristsTestRow> testData, SeasonalityType seasonalityType, boolean withTraining) {
        final ListIterator<HoltWintersAustouristsTestRow> testRows = testData.listIterator();
        HoltWintersAustouristsTestRow firstRow = testRows.next();
        double initLevelEstimate = firstRow.getL();
        double initBaseEstimate = firstRow.getB();
        double[] initSeasonalEstimates = {firstRow.getS4(), firstRow.getS3(), firstRow.getS2(), firstRow.getS1()};
        final HoltWintersParams params = withTraining ?
                buildAustouristsParams(seasonalityType).setInitTrainingMethod(SIMPLE) :
                buildAustouristsParams(seasonalityType, initLevelEstimate, initBaseEstimate, initSeasonalEstimates);

        val subject = new HoltWintersAnomalyDetector();
        subject.init(detectorUUID, params);

        while (testRows.hasNext()) {
            final HoltWintersAustouristsTestRow testRow = testRows.next();
            boolean trainingComplete = subject.isInitialTrainingComplete();
            final double forecastBeforeObservation = subject.getComponents().getForecast();
            AnomalyResult result = subject.classify(new MetricData(metricDefinition, testRow.getY(), epochSecond));
            if (!withTraining || trainingComplete) {
                checkValues(testRow, forecastBeforeObservation, subject, result);
            }
        }
    }

    private void checkValues(HoltWintersAustouristsTestRow testRow, double forecastBeforeObservation, HoltWintersAnomalyDetector subject, AnomalyResult result) {
        assertEquals(testRow.getL(), subject.getComponents().getLevel(), TOLERANCE);
        assertEquals(testRow.getB(), subject.getComponents().getBase(), TOLERANCE);
        double[] expectedReverseSeasonals = {testRow.getS1(), testRow.getS2(), testRow.getS3(), testRow.getS4()};
        double[] actualReverseHistorySeasonals = subject.getComponents().getReverseHistorySeasonals();
        assertArrayEquals(expectedReverseSeasonals, actualReverseHistorySeasonals, TOLERANCE);
        // TODO HW: Record expected anomaly level in R test gen code
        // Assert.assertEquals(testRow.getExpectedLevel(), result.getAnomalyLevel());
        assertEquals(testRow.getYHat(), forecastBeforeObservation, TOLERANCE);
    }

}
