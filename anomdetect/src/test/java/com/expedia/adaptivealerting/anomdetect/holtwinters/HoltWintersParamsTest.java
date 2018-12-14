/*
 * Copyright 2018 Expedia Group, Inc.
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

import org.junit.Test;

public class HoltWintersParamsTest {
    private HoltWintersParams subject = new HoltWintersParams();

    @Test(expected = IllegalArgumentException.class)
    public void testDefaultsAreInvalid() {
        subject.validate();
    }

    @Test
    public void testDefaultsPlusPeriodAreValid() {
        subject.setPeriod(24);
        subject.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSeasonalityType() {
        subject.setSeasonalityType(null);
        subject.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPeriod() {
        subject.setPeriod(-1);
        subject.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidAlphaLessThanZero() {
        subject.setAlpha(-0.1);
        subject.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidAlphaGreaterThanOne() {
        subject.setAlpha(1.1);
        subject.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidBetaLessThanZero() {
        subject.setBeta(-0.1);
        subject.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidBetaGreaterThanOne() {
        subject.setBeta(1.1);
        subject.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidGammaLessThanZero() {
        subject.setGamma(-0.1);
        subject.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidGammaGreaterThanOne() {
        subject.setGamma(1.1);
        subject.validate();
    }

}