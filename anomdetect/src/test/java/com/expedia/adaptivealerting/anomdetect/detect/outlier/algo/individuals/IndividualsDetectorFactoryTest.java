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
package com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.individuals;

import com.expedia.adaptivealerting.anomdetect.detect.AbstractDetectorFactoryTest;
import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class IndividualsDetectorFactoryTest extends AbstractDetectorFactoryTest {
    private static final double TOLERANCE = 0.001;

    @Test
    public void testBuildDetector() {
        val document = readDocument("individuals");
        val factoryUnderTest = new IndividualsDetectorFactory(document);
        val detector = factoryUnderTest.buildDetector();
        val params = detector.getParams();

        assertNotNull(detector);
        assertEquals(IndividualsDetector.class, detector.getClass());
        assertEquals("a6a4d8c4-4102-51fc-a1c7-38aa6f066cca", detector.getUuid().toString());
        assertEquals(3.0, params.getStrongSigmas(), TOLERANCE);
        assertEquals(10.0, params.getInitValue(), TOLERANCE);
        assertEquals(30.0, params.getInitMeanEstimate(), TOLERANCE);
        assertEquals(30, params.getWarmUpPeriod());
    }
}
