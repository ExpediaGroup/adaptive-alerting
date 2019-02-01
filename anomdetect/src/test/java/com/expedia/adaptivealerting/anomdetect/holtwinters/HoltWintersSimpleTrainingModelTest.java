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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.expedia.adaptivealerting.anomdetect.holtwinters.HoltWintersAustouristsTestHelper.ADD_BASE;
import static com.expedia.adaptivealerting.anomdetect.holtwinters.HoltWintersAustouristsTestHelper.ADD_LEVEL;
import static com.expedia.adaptivealerting.anomdetect.holtwinters.HoltWintersAustouristsTestHelper.ADD_SEASONAL;
import static com.expedia.adaptivealerting.anomdetect.holtwinters.HoltWintersAustouristsTestHelper.AUSTOURISTS_FIRST_TWO_SEASONS;
import static com.expedia.adaptivealerting.anomdetect.holtwinters.HoltWintersAustouristsTestHelper.AUSTOURISTS_PERIOD;
import static com.expedia.adaptivealerting.anomdetect.holtwinters.HoltWintersAustouristsTestHelper.MULT_BASE;
import static com.expedia.adaptivealerting.anomdetect.holtwinters.HoltWintersAustouristsTestHelper.MULT_LEVEL;
import static com.expedia.adaptivealerting.anomdetect.holtwinters.HoltWintersAustouristsTestHelper.MULT_SEASONAL;
import static com.expedia.adaptivealerting.anomdetect.holtwinters.HoltWintersAustouristsTestHelper.TOLERANCE;
import static com.expedia.adaptivealerting.anomdetect.holtwinters.HoltWintersAustouristsTestHelper.buildAustouristsParams;
import static com.expedia.adaptivealerting.anomdetect.holtwinters.HoltWintersTrainingMethod.NONE;
import static com.expedia.adaptivealerting.anomdetect.holtwinters.HoltWintersTrainingMethod.SIMPLE;
import static com.expedia.adaptivealerting.anomdetect.holtwinters.SeasonalityType.ADDITIVE;
import static com.expedia.adaptivealerting.anomdetect.holtwinters.SeasonalityType.MULTIPLICATIVE;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class HoltWintersSimpleTrainingModelTest {
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testObserveAndTrain() {
        checkObserveAndTrain(MULTIPLICATIVE, MULT_LEVEL, MULT_BASE, MULT_SEASONAL);
        checkObserveAndTrain(ADDITIVE, ADD_LEVEL, ADD_BASE, ADD_SEASONAL);
    }

    @Test
    public void testNullParamFails() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("params can't be null");
        HoltWintersSimpleTrainingModel subject = new HoltWintersSimpleTrainingModel(buildAustouristsParams(MULTIPLICATIVE));
        subject.observeAndTrain(0, null, null);
    }

    @Test
    public void testNullComponentsFails() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("components can't be null");
        HoltWintersParams params = buildAustouristsParams(MULTIPLICATIVE);
        HoltWintersSimpleTrainingModel subject = new HoltWintersSimpleTrainingModel(params);
        subject.observeAndTrain(0, params, null);
    }

    @Test
    public void testInvalidTrainingMethod() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage(String.format("Expected training method to be %s but was %s", SIMPLE, NONE));
        HoltWintersParams params = buildAustouristsParams(MULTIPLICATIVE);
        HoltWintersOnlineComponents components = new HoltWintersOnlineComponents(params);
        HoltWintersSimpleTrainingModel subject = new HoltWintersSimpleTrainingModel(params);
        subject.observeAndTrain(0, params, components);
    }

    @Test
    public void testExcessTrainingFails() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage(String.format(
                "Training invoked %d times which is greater than the training window of period * 2 (%d * 2 = %d) observations.",
                (AUSTOURISTS_PERIOD * 2) + 1, AUSTOURISTS_PERIOD, AUSTOURISTS_PERIOD * 2));
        HoltWintersParams params = buildAustouristsParams(MULTIPLICATIVE)
                .setInitTrainingMethod(SIMPLE);
        HoltWintersOnlineComponents components = new HoltWintersOnlineComponents(params);
        HoltWintersSimpleTrainingModel subject = new HoltWintersSimpleTrainingModel(params);
        for (int i = 0; i < 9; i++) {
            subject.observeAndTrain(i, params, components);
        }
    }

    private void checkObserveAndTrain(SeasonalityType seasonalityType, double expectedLevel, double expectedBase, double[] expectedSeasonal) {
        HoltWintersParams params = buildAustouristsParams(seasonalityType)
                .setInitTrainingMethod(SIMPLE);
        HoltWintersOnlineComponents components = new HoltWintersOnlineComponents(params);
        HoltWintersSimpleTrainingModel subject = new HoltWintersSimpleTrainingModel(params);
        for (double v : AUSTOURISTS_FIRST_TWO_SEASONS) {
            subject.observeAndTrain(v, params, components);
        }
        assertEquals(expectedLevel, components.getLevel(), TOLERANCE);
        assertEquals(expectedBase, components.getBase(), TOLERANCE);
        assertArrayEquals(expectedSeasonal, components.getSeasonal(), TOLERANCE);
    }

}