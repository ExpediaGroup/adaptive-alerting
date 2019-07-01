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
import com.expedia.adaptivealerting.anomdetect.detect.algo.ForecastingDetector;
import com.expedia.adaptivealerting.anomdetect.forecast.algo.EwmaPointForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.algo.ExponentialWelfordIntervalForecaster;
import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LegacyEwmaFactoryTest extends AbstractDetectorFactoryTest {
    private static final double TOLERANCE = 0.001;

    @Test
    public void testBuildDetector() {
        val document = readDocument("ewma");
        val factoryUnderTest = new LegacyEwmaFactory(document);
        val detector = factoryUnderTest.buildDetector();
        val ewma = (EwmaPointForecaster) detector.getPointForecaster();
        val ewmaParams = ewma.getParams();
        val welford = (ExponentialWelfordIntervalForecaster) detector.getIntervalForecaster();
        val welfordParams = welford.getParams();

        assertEquals(ForecastingDetector.class, detector.getClass());
        assertEquals("3e047348-f837-f615-271c-dce6206f50d6", detector.getUuid().toString());
        assertEquals(AnomalyType.RIGHT_TAILED, detector.getAnomalyType());
        assertEquals(0.20, ewmaParams.getAlpha(), TOLERANCE);
        assertEquals(1.4, ewmaParams.getInitMeanEstimate(), TOLERANCE);
        assertEquals(3.0, welfordParams.getWeakSigmas(), TOLERANCE);
        assertEquals(4.0, welfordParams.getStrongSigmas(), TOLERANCE);
    }
}
