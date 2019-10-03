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

import com.expedia.adaptivealerting.anomdetect.detect.AnomalyType;
import com.expedia.adaptivealerting.anomdetect.forecast.interval.algo.expwelford.ExponentialWelfordIntervalForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.point.algo.pewma.PewmaPointForecaster;
import com.expedia.adaptivealerting.anomdetect.detect.AbstractDetectorFactoryTest;
import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@Deprecated // Use ForecastingDetectorFactory
public class LegacyPewmaFactoryTest extends AbstractDetectorFactoryTest {
    private static final double TOLERANCE = 0.001;

    @Test
    public void testBuildDetector() {
        val document = readDocument("pewma");
        val factoryUnderTest = new LegacyPewmaDetectorFactory(document);
        val detector = factoryUnderTest.buildDetector();
        val pewma = (PewmaPointForecaster) detector.getPointForecaster();
        val pewmaParams = pewma.getParams();
        val welford = (ExponentialWelfordIntervalForecaster) detector.getIntervalForecaster();
        val welfordParams = welford.getParams();

        assertEquals(ForecastingDetector.class, detector.getClass());
        assertEquals("6ec81aa2-2cdc-415e-b4f3-c1beb223ae60", detector.getUuid().toString());
        assertEquals(AnomalyType.RIGHT_TAILED, detector.getAnomalyType());
        assertEquals(0.2, pewmaParams.getAlpha(), TOLERANCE);
        assertEquals(0.18, pewmaParams.getBeta(), TOLERANCE);
        assertEquals(0.44, pewmaParams.getInitMeanEstimate(), TOLERANCE);
        assertEquals(12, pewmaParams.getWarmUpPeriod());
        assertEquals(4.5, welfordParams.getWeakSigmas(), TOLERANCE);
        assertEquals(5, welfordParams.getStrongSigmas(), TOLERANCE);
    }
}
