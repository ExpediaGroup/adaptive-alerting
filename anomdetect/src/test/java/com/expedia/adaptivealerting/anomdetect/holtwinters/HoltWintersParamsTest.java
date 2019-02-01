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
package com.expedia.adaptivealerting.anomdetect.holtwinters;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;

import static com.expedia.adaptivealerting.anomdetect.holtwinters.HoltWintersTrainingMethod.NONE;
import static com.expedia.adaptivealerting.anomdetect.holtwinters.HoltWintersTrainingMethod.SIMPLE;
import static org.junit.Assert.assertEquals;

public class HoltWintersParamsTest {
    public static final int INITIAL_WARM_UP_PERIOD = 0;
    public static final HoltWintersTrainingMethod INITIAL_TRAINING_METHOD = NONE;
    private static int DUMMY_PERIOD = 4;
    private static double[] VALID_MULTIPLICATIVE_SEASONAL_COMPONENT = {1.0, 1.0, 1.0, 1.0};                // Elements add up to period (4)
    private static double[] INVALID_ADDITIVE_SEASONAL_COMPONENT = VALID_MULTIPLICATIVE_SEASONAL_COMPONENT;
    private static double[] VALID_ADDITIVE_SEASONAL_COMPONENT = {-1.0, -1.0, 1.0, 1.0};                    // Elements add up to 0.0
    private static double[] INVALID_MULTIPLICATIVE_SEASONAL_COMPONENT = VALID_ADDITIVE_SEASONAL_COMPONENT;

    private HoltWintersParams subject;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();
    public static final double[] INSUFFICIENT_SEASONAL_ESTIMATES = new double[]{1, 2, 3};

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
        subject.setPeriod(DUMMY_PERIOD);
        subject.validate();
    }

    @Test
    public void testInvalidSeasonalEstimatesLength() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Invalid: initSeasonalEstimates size (" + INSUFFICIENT_SEASONAL_ESTIMATES.length +
                ") must equal period (" + DUMMY_PERIOD + ")");
        subject.setPeriod(DUMMY_PERIOD);
        subject.setInitSeasonalEstimates(INSUFFICIENT_SEASONAL_ESTIMATES);
        subject.validate();
    }

    @Test
    public void testInvalidPeriod() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Required: period value greater than 0");
        subject.setPeriod(-1);
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
    public void testInvalidInitSeasonalEstimateAdditive() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Invalid: Sum of initSeasonalEstimates (4.00) should approximately equal 0 for " +
                "ADDITIVE seasonality type.");
        setUpMinimalValid();
        subject.setSeasonalityType(SeasonalityType.ADDITIVE);
        subject.setInitSeasonalEstimates(INVALID_ADDITIVE_SEASONAL_COMPONENT);
        subject.validate();
    }

    @Test
    public void testInvalidInitSeasonalEstimateMultiplicative() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Invalid: Sum of initSeasonalEstimates (0.00) should be approximately equal to period (4) for " +
                "MULTIPLICATIVE seasonality type.");
        setUpMinimalValid();
        subject.setSeasonalityType(SeasonalityType.MULTIPLICATIVE);
        subject.setInitSeasonalEstimates(INVALID_MULTIPLICATIVE_SEASONAL_COMPONENT);
        subject.validate();
    }

    @Test
    public void testValidInitSeasonalEstimateAdditive() {
        setUpMinimalValid();
        subject.setSeasonalityType(SeasonalityType.ADDITIVE);
        subject.setInitSeasonalEstimates(VALID_ADDITIVE_SEASONAL_COMPONENT);
        subject.validate();
    }

    @Test
    public void testValidInitSeasonalEstimateMultiplicative() {
        setUpMinimalValid();
        subject.setSeasonalityType(SeasonalityType.MULTIPLICATIVE);
        subject.setInitSeasonalEstimates(VALID_MULTIPLICATIVE_SEASONAL_COMPONENT);
        subject.validate();
    }

    @Test
    public void testWarmUpPeriodOverriddenForTrainingMethod() {
        setUpMinimalValid();
        assertEquals(INITIAL_WARM_UP_PERIOD, subject.getWarmUpPeriod());
        assertEquals(INITIAL_TRAINING_METHOD, subject.getInitTrainingMethod());
        subject.validate();
        assertEquals(INITIAL_WARM_UP_PERIOD, subject.getWarmUpPeriod());
        subject.setInitTrainingMethod(SIMPLE);
        subject.validate();
        assertEquals(subject.getPeriod() * 2, subject.getWarmUpPeriod());
    }

    @Test
    public void testGetInitTrainingPeriod() {
        subject.setPeriod(DUMMY_PERIOD);
        subject.setInitTrainingMethod(NONE);
        assertEquals(0, subject.getInitTrainingPeriod());
        subject.setInitTrainingMethod(SIMPLE);
        assertEquals(DUMMY_PERIOD * 2, subject.getInitTrainingPeriod());
    }

    private void setUpMinimalValid() {
        subject.setPeriod(DUMMY_PERIOD);
        subject.setInitSeasonalEstimates(VALID_MULTIPLICATIVE_SEASONAL_COMPONENT);
    }
}
