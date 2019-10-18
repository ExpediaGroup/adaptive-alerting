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
package com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.cusum;

import com.expedia.adaptivealerting.anomdetect.detect.AnomalyType;
import com.expedia.adaptivealerting.anomdetect.detect.AbstractDetectorFactoryTest;
import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CusumDetectorFactoryProviderTest extends AbstractDetectorFactoryTest {
    private static final double TOLERANCE = 0.001;

    @Test
    public void testBuildDetector() {
        val factoryUnderTest = new CusumDetectorFactoryProvider();
        val document = readDocument("cusum");
        val detector = factoryUnderTest.buildDetector(document);
        val params = detector.getParams();

        assertNotNull(detector);
        assertEquals(CusumDetector.class, detector.getClass());
        assertEquals("3ec81aa2-2cdc-415e-b4f3-c1beb223ae60", detector.getUuid().toString());
        assertEquals(120.0, params.getTargetValue(), TOLERANCE);
        assertEquals(0.8, params.getSlackParam(), TOLERANCE);
        assertEquals(110.0, params.getInitMeanEstimate(), TOLERANCE);
        assertEquals(AnomalyType.RIGHT_TAILED, params.getType());
        assertEquals(2.5, params.getWeakSigmas(), TOLERANCE);
        assertEquals(3.5, params.getStrongSigmas(), TOLERANCE);
    }
}
