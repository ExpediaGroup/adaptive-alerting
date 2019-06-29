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
package com.expedia.adaptivealerting.anomdetect.forecast.algo;

import com.expedia.metrics.MetricData;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;

public class MultiplicativeIntervalForecasterTest {
    private static final double TOLERANCE = 0.001;

    private MultiplicativeIntervalForecaster forecasterUnderTest;

    @Mock
    private MetricData metricDataDummy;

    @Before
    public void setUp() {
        val params = new MultiplicativeIntervalForecasterParams()
                .setWeakMultiplier(10.0)
                .setStrongMultiplier(20.0);
        params.validate();

        this.forecasterUnderTest = new MultiplicativeIntervalForecaster(params);

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testForecast() {
        val params = forecasterUnderTest.getParams();
        val pointForecast = 132.4;
        val forecastResult = forecasterUnderTest.forecast(metricDataDummy, pointForecast);

        assertEquals(pointForecast * (1.0 + params.getStrongMultiplier()), forecastResult.getUpperStrong(), TOLERANCE);
        assertEquals(pointForecast * (1.0 + params.getWeakMultiplier()), forecastResult.getUpperWeak(), TOLERANCE);
        assertEquals(pointForecast * (1.0 - params.getWeakMultiplier()), forecastResult.getLowerWeak(), TOLERANCE);
        assertEquals(pointForecast * (1.0 - params.getStrongMultiplier()), forecastResult.getLowerStrong(), TOLERANCE);
    }

    @Test
    public void testValidate() {
        new MultiplicativeIntervalForecasterParams()
                .setWeakMultiplier(3.0)
                .setStrongMultiplier(4.0)
                .validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidate_invalidWeakMultiplier() {
        new MultiplicativeIntervalForecasterParams()
                .setWeakMultiplier(-3.0)
                .setStrongMultiplier(4.0)
                .validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidate_invalidStrongMultiplier() {
        new MultiplicativeIntervalForecasterParams()
                .setWeakMultiplier(3.0)
                .setStrongMultiplier(2.0)
                .validate();
    }
}
