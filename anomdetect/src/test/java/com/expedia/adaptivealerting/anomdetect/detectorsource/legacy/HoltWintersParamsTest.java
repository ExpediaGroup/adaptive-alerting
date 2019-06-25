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
package com.expedia.adaptivealerting.anomdetect.detectorsource.legacy;

import com.expedia.adaptivealerting.anomdetect.outlier.forecast.point.holtwinters.HoltWintersTrainingMethod;
import com.expedia.adaptivealerting.anomdetect.outlier.forecast.point.holtwinters.SeasonalityType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class HoltWintersParamsTest {
    private static final HoltWintersTrainingMethod INITIAL_TRAINING_METHOD = HoltWintersTrainingMethod.NONE;
    private static final double[] INSUFFICIENT_SEASONAL_ESTIMATES = {1, 2, 3};
    private static final double[] DUMMY_SEASONAL_ESTIMATES = {1.1, 0.9, 0.9, 1.1};
    private static final int INITIAL_WARM_UP_PERIOD = 0;
    private static final int DUMMY_FREQUENCY = 4;

    private HoltWintersParams subject;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setUp() {
        subject = new HoltWintersParams();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDefaultsAreInvalid() {
        subject.validate();
    }

    @Test
    public void testMinimalValid() {
        setUpMinimalValid();
        subject.validate();
    }

    @Test
    public void testInvalidSeasonalityType() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Required: seasonalityType one of " + Arrays.toString(SeasonalityType.values()));
        setUpMinimalValid();
        subject.setSeasonalityType(null);
        subject.validate();
    }

    @Test
    public void testInvalidInitTrainingMethod() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Required: initTrainingMethod one of " + Arrays.toString(HoltWintersTrainingMethod.values()));
        setUpMinimalValid();
        subject.setInitTrainingMethod(null);
        subject.validate();
    }

    @Test
    public void testEmptySeasonalEstimatesIsValid() {
        subject.setFrequency(DUMMY_FREQUENCY);
        subject.validate();
    }

    @Test
    public void testInvalidSeasonalEstimatesLength() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Invalid: initSeasonalEstimates size (" + INSUFFICIENT_SEASONAL_ESTIMATES.length +
                ") must equal frequency (" + DUMMY_FREQUENCY + ")");
        subject.setFrequency(DUMMY_FREQUENCY);
        subject.setInitSeasonalEstimates(INSUFFICIENT_SEASONAL_ESTIMATES);
        subject.validate();
    }

    @Test
    public void testInvalidPeriod() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Required: frequency value greater than 0");
        subject.setFrequency(-1);
        subject.validate();
    }

    @Test
    public void testInvalidAlphaLessThanZero() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Required: alpha in the range [0, 1]");
        setUpMinimalValid();
        subject.setAlpha(-0.1);
        subject.validate();
    }

    @Test
    public void testInvalidAlphaGreaterThanOne() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Required: alpha in the range [0, 1]");
        setUpMinimalValid();
        subject.setAlpha(1.1);
        subject.validate();
    }

    @Test
    public void testInvalidBetaLessThanZero() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Required: beta in the range [0, 1]");
        setUpMinimalValid();
        subject.setBeta(-0.1);
        subject.validate();
    }

    @Test
    public void testInvalidBetaGreaterThanOne() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Required: beta in the range [0, 1]");
        setUpMinimalValid();
        subject.setBeta(1.1);
        subject.validate();
    }

    @Test
    public void testInvalidGammaLessThanZero() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Required: gamma in the range [0, 1]");
        setUpMinimalValid();
        subject.setGamma(-0.1);
        subject.validate();
    }

    @Test
    public void testInvalidGammaGreaterThanOne() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Required: gamma in the range [0, 1]");
        setUpMinimalValid();
        subject.setGamma(1.1);
        subject.validate();
    }

    @Test
    public void testWarmUpPeriodOverriddenForTrainingMethod() {
        setUpMinimalValid();
        assertEquals(INITIAL_WARM_UP_PERIOD, subject.getWarmUpPeriod());
        assertEquals(INITIAL_TRAINING_METHOD, subject.getInitTrainingMethod());
        subject.validate();
        assertEquals(INITIAL_WARM_UP_PERIOD, subject.getWarmUpPeriod());
        subject.setInitTrainingMethod(HoltWintersTrainingMethod.SIMPLE);
        subject.validate();
        assertEquals(subject.getFrequency() * 2, subject.getWarmUpPeriod());
    }

    @Test
    public void testGetInitTrainingPeriod() {
        subject.setFrequency(DUMMY_FREQUENCY);
        subject.setInitTrainingMethod(HoltWintersTrainingMethod.NONE);
        assertEquals(0, subject.calculateInitTrainingPeriod());
        subject.setInitTrainingMethod(HoltWintersTrainingMethod.SIMPLE);
        assertEquals(DUMMY_FREQUENCY * 2, subject.calculateInitTrainingPeriod());
    }

    private void setUpMinimalValid() {
        subject.setFrequency(DUMMY_FREQUENCY);
        subject.setInitSeasonalEstimates(DUMMY_SEASONAL_ESTIMATES);
    }
}
