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
package com.expedia.adaptivealerting.anomdetect.forecast.point;

import com.expedia.adaptivealerting.anomdetect.comp.legacy.HoltWintersParams;
import com.expedia.adaptivealerting.anomdetect.forecast.point.holtwinters.HoltWintersAustouristsTestRow;
import com.expedia.adaptivealerting.anomdetect.forecast.point.holtwinters.HoltWintersTrainingMethod;
import com.expedia.adaptivealerting.anomdetect.forecast.point.holtwinters.SeasonalityType;
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

import static com.expedia.adaptivealerting.anomdetect.forecast.point.holtwinters.HoltWintersAustouristsTestHelper.AUSTOURISTS_ADD_DATA;
import static com.expedia.adaptivealerting.anomdetect.forecast.point.holtwinters.HoltWintersAustouristsTestHelper.AUSTOURISTS_MULT_DATA;
import static com.expedia.adaptivealerting.anomdetect.forecast.point.holtwinters.HoltWintersAustouristsTestHelper.TOLERANCE;
import static com.expedia.adaptivealerting.anomdetect.forecast.point.holtwinters.HoltWintersAustouristsTestHelper.buildAustouristsParams;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Tests Holt-Winters functionality by comparing with data generated from Hyndman's R "fpp2" library - see GenerateAustouristsTests.R
 */
public final class HoltWintersForecasterTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private MetricDefinition metricDef;
    private long epochSecond;

    @Before
    public void setUp() {
        this.metricDef = new MetricDefinition("some-key");
        this.epochSecond = Instant.now().getEpochSecond();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInit_alphaOutOfRange() {
        new HoltWintersParams()
                .setFrequency(24)
                .setAlpha(2.0)
                .validate();
    }

    @Test
    public void testAdditiveProvidingInitialEstimates() {
        doAustouristsTest(AUSTOURISTS_ADD_DATA, SeasonalityType.ADDITIVE, false);
    }

    @Test
    public void testMultiplicativeProvidingInitialEstimates() {
        doAustouristsTest(AUSTOURISTS_MULT_DATA, SeasonalityType.MULTIPLICATIVE, false);
    }

    @Test
    public void testAdditiveWithTraining() {
        doAustouristsTest(AUSTOURISTS_ADD_DATA, SeasonalityType.ADDITIVE, true);
    }

    @Test
    public void testMultiplicativeWithTraining() {
        doAustouristsTest(AUSTOURISTS_MULT_DATA, SeasonalityType.MULTIPLICATIVE, true);
    }

    private void doAustouristsTest(List<HoltWintersAustouristsTestRow> testData, SeasonalityType seasonalityType, boolean withTraining) {
        final ListIterator<HoltWintersAustouristsTestRow> testRows = testData.listIterator();
        HoltWintersAustouristsTestRow firstRow = testRows.next();
        double initLevelEstimate = firstRow.getL();
        double initBaseEstimate = firstRow.getB();
        double[] initSeasonalEstimates = {firstRow.getS4(), firstRow.getS3(), firstRow.getS2(), firstRow.getS1()};
        final HoltWintersForecaster.Params params = withTraining ?
                buildAustouristsParams(seasonalityType).setInitTrainingMethod(HoltWintersTrainingMethod.SIMPLE) :
                buildAustouristsParams(seasonalityType, initLevelEstimate, initBaseEstimate, initSeasonalEstimates);

        val subject = new HoltWintersForecaster(params);

        while (testRows.hasNext()) {
            final HoltWintersAustouristsTestRow testRow = testRows.next();
            boolean trainingComplete = subject.isInitialTrainingComplete();
            final double forecastBeforeObservation = subject.getComponents().getForecast();
            PointForecast result = subject.forecast(new MetricData(metricDef, testRow.getY(), epochSecond));
            if (!withTraining || trainingComplete) {
                checkValues(testRow, forecastBeforeObservation, subject, result);
            }
        }
    }

    private void checkValues(HoltWintersAustouristsTestRow testRow, double forecastBeforeObservation, HoltWintersForecaster subject, PointForecast result) {
//        assertEquals(testRow.getL(), subject.getComponents().getLevel(), TOLERANCE);
//        assertEquals(testRow.getB(), subject.getComponents().getBase(), TOLERANCE);
        double[] expectedReverseSeasonals = {testRow.getS1(), testRow.getS2(), testRow.getS3(), testRow.getS4()};
        double[] actualReverseHistorySeasonals = subject.getComponents().getReverseHistorySeasonals();
        assertArrayEquals(expectedReverseSeasonals, actualReverseHistorySeasonals, TOLERANCE);
        // TODO HW: Record expected anomaly level in R test gen code
        // Assert.assertEquals(testRow.getExpectedLevel(), result.getAnomalyLevel());
        assertEquals(testRow.getYHat(), forecastBeforeObservation, TOLERANCE);
    }
}
