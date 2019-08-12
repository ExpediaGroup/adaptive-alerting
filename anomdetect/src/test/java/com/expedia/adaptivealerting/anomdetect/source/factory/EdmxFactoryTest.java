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

import com.expedia.adaptivealerting.anomdetect.detect.breakout.algo.EdmxDetector;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Slf4j
public class EdmxFactoryTest extends AbstractDetectorFactoryTest {
    private static final double TOLERANCE = 0.01;

    @Test
    public void testBuildDetector() {
        val document = readDocument("edmx");
        val factoryUnderTest = new EdmxFactory(document);
        val detector = factoryUnderTest.buildDetector();
        val hyperparams = detector.getHyperparams();

        assertNotNull(detector);
        assertEquals("9d934a88-f89b-4793-956a-bede72c3081f", detector.getUuid().toString());
        assertEquals(EdmxDetector.class, detector.getClass());
        assertEquals(24, hyperparams.getBufferSize());
        assertEquals(6, hyperparams.getDelta());
        assertEquals(199, hyperparams.getNumPerms());
        assertEquals(0.01, hyperparams.getStrongAlpha(), TOLERANCE);
        assertEquals(0.05, hyperparams.getWeakAlpha(), TOLERANCE);
    }

    @Test(expected = RuntimeException.class)
    public void testBuild_invalidUuid() {
        readDocument("edmx-invalid-uuid");
    }

    @Test(expected = RuntimeException.class)
    public void testBuild_invalidHyperparams() {
        val document = readDocument("edmx-invalid-hyperparams");
        val factoryUnderTest = new EdmxFactory(document);
        factoryUnderTest.buildDetector();
    }
}
