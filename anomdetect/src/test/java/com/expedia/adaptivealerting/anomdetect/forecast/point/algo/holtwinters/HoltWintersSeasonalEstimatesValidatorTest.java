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
package com.expedia.adaptivealerting.anomdetect.forecast.point.algo.holtwinters;

import com.expedia.adaptivealerting.anomdetect.forecast.point.algo.holtwinters.HoltWintersSeasonalEstimatesValidator;
import com.expedia.adaptivealerting.anomdetect.forecast.point.algo.holtwinters.HoltWintersSeasonalityType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class HoltWintersSeasonalEstimatesValidatorTest {
    private static int DUMMY_FREQUENCY = 4;
    private static final double[] INSUFFICIENT_SEASONAL_ESTIMATES = new double[]{1, 2, 3};
    private static double[] VALID_ADDITIVE_SEASONAL_COMPONENT = {100_000, 0, 0, -101_000}; // Elements add up to -1,000 which is within 1% of largest absolute element (±101,000/100 = ±1,010)
    private static double[] INVALID_ADDITIVE_SEASONAL_COMPONENT = {100_000, 0, 0, -101_100}; // Elements add up to -1,100 which is outside 1% of largest absolute element (±101,000/100 = ±1,010)
    private static double[] VALID_MULTIPLICATIVE_SEASONAL_COMPONENT = {1.001001, 1, 1, (1 - 0.001)}; // Elements add up 4.000,001 which is within 4 ± 1% of largest element's distance from 1.0 (between 3.999,989,990 <--> 4.000,010,010
    private static double[] INVALID_MULTIPLICATIVE_SEASONAL_COMPONENT = {1.001_100, 1, 1, (1 - 0.001)}; // Elements add up 4.000,000,020 which is within 4 ±1% of largest element distance from 1.0 (0.000,020 / 100) --> 4±0.000,000,200 i.e. 3.999,999,800 <--> 4.000,000,200

    private HoltWintersSeasonalEstimatesValidator subject;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setUp() {
        subject = new HoltWintersSeasonalEstimatesValidator();
    }

    @Test
    public void testEmptySeasonalEstimatesIsValid() {
        subject.validate(new double[]{}, DUMMY_FREQUENCY, HoltWintersSeasonalityType.MULTIPLICATIVE);
    }

    @Test
    public void testValidMultiplicativeSeasonalEstimatesIsValid() {
        subject.validate(VALID_MULTIPLICATIVE_SEASONAL_COMPONENT, DUMMY_FREQUENCY, HoltWintersSeasonalityType.MULTIPLICATIVE);
    }

    @Test
    public void testValidAdditiveSeasonalEstimatesIsValid() {
        subject.validate(VALID_ADDITIVE_SEASONAL_COMPONENT, DUMMY_FREQUENCY, HoltWintersSeasonalityType.ADDITIVE);
    }

    @Test
    public void testInvalidSeasonalEstimatesLength() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage(String.format("Invalid: initSeasonalEstimates size (%d) must equal frequency (%d)",
                INSUFFICIENT_SEASONAL_ESTIMATES.length, DUMMY_FREQUENCY));
        subject.validate(INSUFFICIENT_SEASONAL_ESTIMATES, DUMMY_FREQUENCY, HoltWintersSeasonalityType.MULTIPLICATIVE);
    }

    @Test
    public void testInvalidInitSeasonalEstimateAdditive() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Invalid: Sum of initSeasonalEstimates (-1,100) was outside accepted tolerance. Sum should be 0 with a tolerance within 1% of largest seasonal estimate distance from 0 (±1,011), for ADDITIVE seasonality type.");
        subject.validate(INVALID_ADDITIVE_SEASONAL_COMPONENT, DUMMY_FREQUENCY, HoltWintersSeasonalityType.ADDITIVE);
    }

    @Test
    public void testInvalidInitSeasonalEstimateMultiplicative() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Invalid: Sum of initSeasonalEstimates (4.0001) was outside accepted tolerance. Sum should equal 'frequency' with a tolerance within 1% of largest seasonal estimate distance from 1 (4 ± 0.000011), for MULTIPLICATIVE seasonality type.");
        subject.validate(INVALID_MULTIPLICATIVE_SEASONAL_COMPONENT, DUMMY_FREQUENCY, HoltWintersSeasonalityType.MULTIPLICATIVE);
    }

}
