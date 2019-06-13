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
package com.expedia.adaptivealerting.anomdetect.outlier.forecast.point.holtwinters;

import com.expedia.adaptivealerting.anomdetect.outlier.forecast.point.HoltWintersForecaster;
import org.junit.Assert;
import org.junit.Test;

import static com.expedia.adaptivealerting.anomdetect.outlier.forecast.point.holtwinters.HoltWintersAustouristsTestHelper.buildAustouristsParams;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class HoltWintersOnlineComponentsTest {
    private static final double TOLERANCE = 0;

    @Test
    public void testInitialValues() {
        double initLevelEstimate = 80000;
        double initBaseEstimate = 2;
        double[] initSeasonalEstimates = {1, 2, 3, 4};
        final HoltWintersForecaster.Params params = HoltWintersAustouristsTestHelper.buildAustouristsParams(SeasonalityType.MULTIPLICATIVE, initLevelEstimate, initBaseEstimate, initSeasonalEstimates);
        HoltWintersOnlineComponents subject = new HoltWintersOnlineComponents(params);
//        assertEquals(params, subject.getParams());
        assertEquals(0, subject.getN());
        assertEquals(initLevelEstimate, subject.getLevel(), TOLERANCE);
        assertEquals(initBaseEstimate, subject.getBase(), TOLERANCE);
        assertArrayEquals(initSeasonalEstimates, subject.getSeasonal(), TOLERANCE);
        assertEquals(0, subject.getCurrentSeasonalIndex());
        assertArrayEquals(new double[]{4, 3, 2, 1}, subject.getReverseHistorySeasonals(), TOLERANCE);
        for (int i = 0; i < initSeasonalEstimates.length; i++) {
            double initSeasonalEstimate = initSeasonalEstimates[i];
            assertEquals(0, subject.getSeasonalStandardDeviation(i), TOLERANCE);
        }
        // Initial forecast will be set by the algorithm, we expect it to not be set before then
        assertEquals(Double.NaN, subject.getForecast(), TOLERANCE);
    }

    @Test
    public void testConstructorNoInitSeasonalsMultiplicative() {
        final HoltWintersForecaster.Params params = HoltWintersAustouristsTestHelper.buildAustouristsParams(SeasonalityType.MULTIPLICATIVE);
        HoltWintersOnlineComponents subject = new HoltWintersOnlineComponents(params);
        Assert.assertArrayEquals(HoltWintersAustouristsTestHelper.MULTIPLICATIVE_IDENTITY_SEASONALS, subject.getSeasonal(), TOLERANCE);
    }

    @Test
    public void testConstructorNoInitSeasonalsAdditive() {
        final HoltWintersForecaster.Params params = HoltWintersAustouristsTestHelper.buildAustouristsParams(SeasonalityType.ADDITIVE);
        HoltWintersOnlineComponents subject = new HoltWintersOnlineComponents(params);
        Assert.assertArrayEquals(HoltWintersAustouristsTestHelper.ADDITIVE_IDENTITY_SEASONALS, subject.getSeasonal(), TOLERANCE);
    }

    @Test(expected = IllegalStateException.class)
    public void testConstructorWithInvalidSeasonals() {
        double initLevelEstimate = 80000;
        double initBaseEstimate = 2;
        double[] initSeasonalEstimates = {1, 2, 3};
        final HoltWintersForecaster.Params params = HoltWintersAustouristsTestHelper.buildAustouristsParams(SeasonalityType.MULTIPLICATIVE, initLevelEstimate, initBaseEstimate, initSeasonalEstimates);
        new HoltWintersOnlineComponents(params);
    }
}
