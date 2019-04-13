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
package com.expedia.adaptivealerting.anomdetect.forecast.point.holtwinters;

import com.expedia.adaptivealerting.anomdetect.forecast.point.HoltWintersForecaster;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class HoltWintersSimpleTrainingModelTest {
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testObserveAndTrain() {
        checkObserveAndTrain(SeasonalityType.MULTIPLICATIVE, HoltWintersAustouristsTestHelper.MULT_LEVEL, HoltWintersAustouristsTestHelper.MULT_BASE, HoltWintersAustouristsTestHelper.MULT_SEASONAL);
        checkObserveAndTrain(SeasonalityType.ADDITIVE, HoltWintersAustouristsTestHelper.ADD_LEVEL, HoltWintersAustouristsTestHelper.ADD_BASE, HoltWintersAustouristsTestHelper.ADD_SEASONAL);
    }

    @Test
    public void testNullParamFails() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("params can't be null");
        HoltWintersSimpleTrainingModel subject = new HoltWintersSimpleTrainingModel(HoltWintersAustouristsTestHelper.buildAustouristsParams(SeasonalityType.MULTIPLICATIVE));
        subject.observeAndTrain(0, null, null);
    }

    @Test
    public void testNullComponentsFails() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("components can't be null");
        HoltWintersForecaster.Params params = HoltWintersAustouristsTestHelper.buildAustouristsParams(SeasonalityType.MULTIPLICATIVE);
        HoltWintersSimpleTrainingModel subject = new HoltWintersSimpleTrainingModel(params);
        subject.observeAndTrain(0, params, null);
    }

    @Test
    public void testInvalidTrainingMethod() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage(String.format("Expected training method to be %s but was %s", HoltWintersTrainingMethod.SIMPLE, HoltWintersTrainingMethod.NONE));
        HoltWintersForecaster.Params params = HoltWintersAustouristsTestHelper.buildAustouristsParams(SeasonalityType.MULTIPLICATIVE);
        HoltWintersOnlineComponents components = new HoltWintersOnlineComponents(params);
        HoltWintersSimpleTrainingModel subject = new HoltWintersSimpleTrainingModel(params);
        subject.observeAndTrain(0, params, components);
    }

    @Test
    public void testExcessTrainingFails() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage(String.format(
                "Training invoked %d times which is greater than the training window of frequency * 2 (%d * 2 = %d) observations.",
                (HoltWintersAustouristsTestHelper.AUSTOURISTS_FREQUENCY * 2) + 1, HoltWintersAustouristsTestHelper.AUSTOURISTS_FREQUENCY, HoltWintersAustouristsTestHelper.AUSTOURISTS_FREQUENCY * 2));
        HoltWintersForecaster.Params params = HoltWintersAustouristsTestHelper.buildAustouristsParams(SeasonalityType.MULTIPLICATIVE)
                .setInitTrainingMethod(HoltWintersTrainingMethod.SIMPLE);
        HoltWintersOnlineComponents components = new HoltWintersOnlineComponents(params);
        HoltWintersSimpleTrainingModel subject = new HoltWintersSimpleTrainingModel(params);
        for (int i = 0; i < 9; i++) {
            subject.observeAndTrain(i, params, components);
        }
    }

    private void checkObserveAndTrain(SeasonalityType seasonalityType, double expectedLevel, double expectedBase, double[] expectedSeasonal) {
        HoltWintersForecaster.Params params = HoltWintersAustouristsTestHelper.buildAustouristsParams(seasonalityType)
                .setInitTrainingMethod(HoltWintersTrainingMethod.SIMPLE);
        HoltWintersOnlineComponents components = new HoltWintersOnlineComponents(params);
        HoltWintersSimpleTrainingModel subject = new HoltWintersSimpleTrainingModel(params);
        for (double v : HoltWintersAustouristsTestHelper.AUSTOURISTS_FIRST_TWO_SEASONS) {
            subject.observeAndTrain(v, params, components);
        }
        Assert.assertEquals(expectedLevel, components.getLevel(), HoltWintersAustouristsTestHelper.TOLERANCE);
        Assert.assertEquals(expectedBase, components.getBase(), HoltWintersAustouristsTestHelper.TOLERANCE);
        Assert.assertArrayEquals(expectedSeasonal, components.getSeasonal(), HoltWintersAustouristsTestHelper.TOLERANCE);
    }

}
