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
package com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.forecasting;

import com.expedia.adaptivealerting.anomdetect.detect.AbstractDetectorFactoryTest;
import com.expedia.adaptivealerting.anomdetect.detect.AnomalyType;
import com.expedia.adaptivealerting.anomdetect.forecast.interval.algo.multiplicative.MultiplicativeIntervalForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.point.algo.seasonalnaive.SeasonalNaivePointForecaster;
import lombok.val;
import org.junit.Test;

import static com.expedia.adaptivealerting.anomdetect.forecast.point.algo.seasonalnaive.SeasonalNaivePointForecasterParams.DEFAULT_MISSING_VALUE_PLACEHOLDER;
import static org.junit.Assert.assertEquals;

@Deprecated // Use ForecastingDetectorFactory
public class LegacySeasonalNaiveDetectorFactoryProviderTest extends AbstractDetectorFactoryTest {
    private static final double TOLERANCE = 0.001;
    private static final int ONE_WEEK_IN_SECONDS = 60 * 24 * 7;
    private static final int ONE_MINUTE_IN_SECONDS = 60;

    @Test
    public void testBuildDetector() {
        val factoryUnderTest = new LegacySeasonalNaiveDetectorFactoryProvider();
        val document = readDocument("seasonal-naive");
        val detector = factoryUnderTest.buildDetector(document);
        val seasonalNaive = (SeasonalNaivePointForecaster) detector.getPointForecaster();
        val seasonalNaiveParams = seasonalNaive.getParams();
        val multiplicative = (MultiplicativeIntervalForecaster) detector.getIntervalForecaster();
        val multiplicativeParams = multiplicative.getParams();

        assertEquals(ForecastingDetector.class, detector.getClass());
        assertEquals("6ec81aa2-2cdc-415e-b4f3-abc123cba321", detector.getUuid().toString());
        assertEquals(AnomalyType.RIGHT_TAILED, detector.getAnomalyType());
        assertEquals(ONE_WEEK_IN_SECONDS, seasonalNaiveParams.getCycleLength(), TOLERANCE);
        assertEquals(ONE_MINUTE_IN_SECONDS, seasonalNaiveParams.getIntervalLength(), TOLERANCE);
        assertEquals(DEFAULT_MISSING_VALUE_PLACEHOLDER, seasonalNaiveParams.getMissingValuePlaceholder(), TOLERANCE);
        assertEquals(3, multiplicativeParams.getWeakMultiplier(), TOLERANCE);
        assertEquals(4, multiplicativeParams.getStrongMultiplier(), TOLERANCE);
    }
}
