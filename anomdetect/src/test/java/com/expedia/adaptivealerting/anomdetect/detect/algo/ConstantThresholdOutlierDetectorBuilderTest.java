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
import com.expedia.adaptivealerting.anomdetect.detect.AnomalyType;
import com.expedia.adaptivealerting.anomdetect.detect.algo.ConstantThresholdOutlierDetector;
import com.expedia.adaptivealerting.anomdetect.detect.algo.ConstantThresholdOutlierDetectorBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Slf4j
public class ConstantThresholdOutlierDetectorBuilderTest extends AbstractDetectorBuilderTest {
    private static final double TOLERANCE = 0.001;

    private ConstantThresholdOutlierDetectorBuilder builderUnderTest;

    @Before
    public void setUp() {
        this.builderUnderTest = new ConstantThresholdOutlierDetectorBuilder();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuild_nullDocument() {
        builderUnderTest.build(null);
    }

    @Test
    public void testBuild() {
        val document = readDocument("constant-threshold");
        val detector = builderUnderTest.build(document);
        val params = detector.getParams();
        val thresholds = params.getThresholds();

        assertNotNull(detector);
        assertEquals("e2e290a0-d1c1-471e-9d72-79d43282cfbd", detector.getUuid().toString());
        assertEquals(ConstantThresholdOutlierDetector.class, detector.getClass());
        Assert.assertEquals(AnomalyType.RIGHT_TAILED, params.getType());
        assertEquals(16666.0, thresholds.getUpperStrong(), TOLERANCE);
        assertEquals(2161.0, thresholds.getUpperWeak(), TOLERANCE);
    }

    @Test(expected = RuntimeException.class)
    public void testBuild_invalidUuid() {
        readDocument("constant-threshold-invalid-uuid");
    }

    @Test(expected = RuntimeException.class)
    public void testBuild_invalidParams() {
        val document = readDocument("constant-threshold-invalid-params");
        builderUnderTest.build(document);
    }
}
