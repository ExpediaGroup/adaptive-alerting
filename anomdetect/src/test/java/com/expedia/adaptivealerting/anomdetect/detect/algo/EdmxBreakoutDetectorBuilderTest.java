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
package com.expedia.adaptivealerting.anomdetect.detect.algo;

import com.expedia.adaptivealerting.anomdetect.detect.AbstractDetectorBuilderTest;
import com.expedia.adaptivealerting.anomdetect.detect.algo.EdmxBreakoutDetector;
import com.expedia.adaptivealerting.anomdetect.detect.algo.EdmxBreakoutDetectorBuilder;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EdmxBreakoutDetectorBuilderTest extends AbstractDetectorBuilderTest {
    private static final double TOLERANCE = 0.01;

    private EdmxBreakoutDetectorBuilder builderUnderTest;

    @Before
    public void setUp() {
        this.builderUnderTest = new EdmxBreakoutDetectorBuilder();
    }

    @Test
    public void testBuild() {
        val document = readDocument("edmx");
        val detector = builderUnderTest.build(document);
        val hyperparams = detector.getHyperparams();

        assertNotNull(detector);
        assertEquals("9d934a88-f89b-4793-956a-bede72c3081f", detector.getUuid().toString());
        assertEquals(EdmxBreakoutDetector.class, detector.getClass());
        assertEquals(24, hyperparams.getBufferSize());
        assertEquals(6, hyperparams.getDelta());
        assertEquals(199, hyperparams.getNumPerms());
        assertEquals(0.01, hyperparams.getAlpha(), TOLERANCE);
    }

    @Test(expected = RuntimeException.class)
    public void testBuild_invalidUuid() {
        readDocument("edmx-invalid-uuid");
    }

    @Test(expected = RuntimeException.class)
    public void testBuild_invalidHyperparams() {
        val document = readDocument("edmx-invalid-hyperparams");
        builderUnderTest.build(document);
    }
}
