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
package com.expedia.adaptivealerting.anomdetect.forecast.point.algo;

import com.expedia.adaptivealerting.anomdetect.forecast.point.PointForecast;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import lombok.val;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class NaivePointForecasterTest {
    private static final double TOLERANCE = 0.001;

    private NaivePointForecaster forecasterUnderTest = new NaivePointForecaster();
    private Random random = new Random();
    private MetricDefinition metricDef = new MetricDefinition("some-key");

    @Test
    public void testForecast() {
        double currValue = random.nextDouble();
        MetricData metricData = new MetricData(metricDef, currValue, 0);
        PointForecast forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);

        for (int i = 0; i < 10; i++) {
            val prevValue = currValue;
            currValue = random.nextDouble();
            metricData = new MetricData(metricDef, currValue, 0);
            forecast = forecasterUnderTest.forecast(metricData);
            assertEquals(forecast.getValue(), prevValue, TOLERANCE);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForecast_nullMetricData() {
        forecasterUnderTest.forecast(null);
    }
}
