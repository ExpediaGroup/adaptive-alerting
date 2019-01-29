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
import org.junit.Test;

public class HoltWintersParamsTest {
    private static int DUMMY_PERIOD = 4;
    private static double[] VALID_MULTIPLICATIVE_SEASONAL_COMPONENT = {1.0, 1.0, 1.0, 1.0};                // Elements add up to period (4)
    private static double[] INVALID_ADDITIVE_SEASONAL_COMPONENT = VALID_MULTIPLICATIVE_SEASONAL_COMPONENT;
    private static double[] VALID_ADDITIVE_SEASONAL_COMPONENT = {-1.0, -1.0, 1.0, 1.0};                    // Elements add up to 0.0
    private static double[] INVALID_MULTIPLICATIVE_SEASONAL_COMPONENT = VALID_ADDITIVE_SEASONAL_COMPONENT;

    private HoltWintersParams subject;

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

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSeasonalityType() {
        setUpMinimalValid();
        subject.setSeasonalityType(null);
        subject.validate();
    }

    @Test
    public void testEmptySeasonalEstimatesIsValid() {
        subject.setPeriod(DUMMY_PERIOD);
        subject.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSeasonalEstimatesLength() {
        subject.setPeriod(DUMMY_PERIOD);
        subject.setInitSeasonalEstimates(new double[]{1, 2, 3});
        subject.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPeriod() {
        subject.setPeriod(-1);
        subject.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidAlphaLessThanZero() {
        setUpMinimalValid();
        subject.setAlpha(-0.1);
        subject.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidAlphaGreaterThanOne() {
        setUpMinimalValid();
        subject.setAlpha(1.1);
        subject.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidBetaLessThanZero() {
        setUpMinimalValid();
        subject.setBeta(-0.1);
        subject.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidBetaGreaterThanOne() {
        setUpMinimalValid();
        subject.setBeta(1.1);
        subject.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidGammaLessThanZero() {
        setUpMinimalValid();
        subject.setGamma(-0.1);
        subject.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidGammaGreaterThanOne() {
        setUpMinimalValid();
        subject.setGamma(1.1);
        subject.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidInitSeasonalEstimateAdditive() {
        setUpMinimalValid();
        subject.setSeasonalityType(SeasonalityType.ADDITIVE);
        subject.setInitSeasonalEstimates(INVALID_ADDITIVE_SEASONAL_COMPONENT);
        subject.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidInitSeasonalEstimateMultiplicative() {
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


    private void setUpMinimalValid() {
        subject.setPeriod(DUMMY_PERIOD);
        subject.setInitSeasonalEstimates(VALID_MULTIPLICATIVE_SEASONAL_COMPONENT);
    }
}
