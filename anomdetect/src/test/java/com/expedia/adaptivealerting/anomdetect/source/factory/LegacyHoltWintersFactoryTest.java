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
package com.expedia.adaptivealerting.anomdetect.source.factory;

import com.expedia.adaptivealerting.anomdetect.detect.AnomalyType;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.ForecastingDetector;
import com.expedia.adaptivealerting.anomdetect.forecast.interval.algo.ExponentialWelfordIntervalForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.point.algo.HoltWintersPointForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.point.algo.HoltWintersSeasonalityType;
import com.expedia.adaptivealerting.anomdetect.forecast.point.algo.HoltWintersTrainingMethod;
import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LegacyHoltWintersFactoryTest extends AbstractDetectorFactoryTest {
    private static final double TOLERANCE = 0.001;

    @Test
    public void testBuildDetector() {
        val document = readDocument("holt-winters");
        val factoryUnderTest = new LegacyHoltWintersFactory(document);
        val detector = factoryUnderTest.buildDetector();
        val hw = (HoltWintersPointForecaster) detector.getPointForecaster();
        val hwParams = hw.getParams();
        val welford = (ExponentialWelfordIntervalForecaster) detector.getIntervalForecaster();
        val welfordParams = welford.getParams();

        assertEquals(ForecastingDetector.class, detector.getClass());
        assertEquals("a63c2128-113a-8fd7-942d-f8ae228b61b0", detector.getUuid().toString());
        assertEquals(AnomalyType.RIGHT_TAILED, detector.getAnomalyType());
        assertEquals(2016, hwParams.getFrequency());
        assertEquals(0.15, hwParams.getAlpha(), TOLERANCE);
        assertEquals(0.10, hwParams.getBeta(), TOLERANCE);
        assertEquals(0.20, hwParams.getGamma(), TOLERANCE);
        assertEquals(0, hwParams.getWarmUpPeriod());
        assertEquals(HoltWintersSeasonalityType.MULTIPLICATIVE, hwParams.getSeasonalityType());
        assertEquals(HoltWintersTrainingMethod.NONE, hwParams.getInitTrainingMethod());
        assertEquals(0.0, hwParams.getInitBaseEstimate(), TOLERANCE);
        assertEquals(0.0, hwParams.getInitLevelEstimate(), TOLERANCE);
        assertEquals(0, hwParams.getInitSeasonalEstimates().length);
        assertEquals(3.0, welfordParams.getWeakSigmas(), TOLERANCE);
        assertEquals(4.0, welfordParams.getStrongSigmas(), TOLERANCE);
    }
}
