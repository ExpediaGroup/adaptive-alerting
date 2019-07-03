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
package com.expedia.adaptivealerting.anomdetect.forecast.interval.algo;

import com.expedia.metrics.MetricData;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;

public class PowerLawIntervalForecasterTest {
    private static final double TOLERANCE = 0.001;

    private PowerLawIntervalForecaster forecasterUnderTest;

    @Mock
    private MetricData metricData;

    @Before
    public void setUp() {
        val params = new PowerLawIntervalForecasterParams()
                .setAlpha(0.5)
                .setBeta(0.85)
                .setWeakMultiplier(3.0)
                .setStrongMultiplier(4.0);
        params.validate();

        this.forecasterUnderTest = new PowerLawIntervalForecaster(params);

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testForecast() {
        val params = forecasterUnderTest.getParams();
        val pointForecast = 132.4;
        val forecastResult = forecasterUnderTest.forecast(metricData, pointForecast);

        val width = params.getAlpha() * Math.pow(pointForecast, params.getBeta());
        val weakWidth = params.getWeakMultiplier() * width;
        val strongWidth = params.getStrongMultiplier() * width;

        assertEquals(pointForecast + strongWidth, forecastResult.getUpperStrong(), TOLERANCE);
        assertEquals(pointForecast + weakWidth, forecastResult.getUpperWeak(), TOLERANCE);
        assertEquals(pointForecast - weakWidth, forecastResult.getLowerWeak(), TOLERANCE);
        assertEquals(pointForecast - strongWidth, forecastResult.getLowerStrong(), TOLERANCE);
    }

    @Test
    public void testValidate() {
        new PowerLawIntervalForecasterParams()
                .setAlpha(0.15)
                .setBeta(0.85)
                .setWeakMultiplier(3.0)
                .setStrongMultiplier(4.0)
                .validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidate_invalidAlpha() {
        new PowerLawIntervalForecasterParams()
                .setAlpha(-0.15)
                .setBeta(0.85)
                .setWeakMultiplier(3.0)
                .setStrongMultiplier(4.0)
                .validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidate_invalidBeta() {
        new PowerLawIntervalForecasterParams()
                .setAlpha(0.15)
                .setBeta(-0.85)
                .setWeakMultiplier(3.0)
                .setStrongMultiplier(4.0)
                .validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidate_invalidWeakMultiplier() {
        new PowerLawIntervalForecasterParams()
                .setAlpha(0.15)
                .setBeta(0.85)
                .setWeakMultiplier(-3.0)
                .setStrongMultiplier(4.0)
                .validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidate_invalidStrongMultiplier() {
        new PowerLawIntervalForecasterParams()
                .setAlpha(0.15)
                .setBeta(0.85)
                .setWeakMultiplier(3.0)
                .setStrongMultiplier(2.0)
                .validate();
    }
}
