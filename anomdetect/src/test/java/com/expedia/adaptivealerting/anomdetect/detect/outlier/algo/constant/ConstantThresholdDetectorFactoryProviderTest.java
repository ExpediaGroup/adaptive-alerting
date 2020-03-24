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
package com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.constant;

import com.expedia.adaptivealerting.anomdetect.detect.AbstractDetectorFactoryTest;
import com.expedia.adaptivealerting.anomdetect.detect.AnomalyType;
import com.expedia.adaptivealerting.anomdetect.filter.PostDetectionFilter;
import com.expedia.adaptivealerting.anomdetect.filter.PreDetectionFilter;
import com.expedia.adaptivealerting.anomdetect.filter.algo.post.MOfNAggregationFilter;
import com.expedia.adaptivealerting.anomdetect.filter.algo.post.PassThroughPostDetectionFilter;
import com.expedia.adaptivealerting.anomdetect.filter.algo.pre.HourOfDayDetectionFilter;
import com.expedia.adaptivealerting.anomdetect.filter.algo.pre.PassThroughPreDetectionFilter;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

@Slf4j
public class ConstantThresholdDetectorFactoryProviderTest extends AbstractDetectorFactoryTest {
    private static final double TOLERANCE = 0.001;

    @Test
    public void testBuildDetector() {
        val factoryUnderTest = new ConstantThresholdDetectorFactoryProvider();
        val document = readDocument("constant-threshold");
        val detector = factoryUnderTest.buildDetector(document);
        val params = detector.getParams();
        val thresholds = params.getThresholds();

        assertNotNull(detector);
        assertSame(ConstantThresholdDetector.class, detector.getClass());
        assertEquals("e2e290a0-d1c1-471e-9d72-79d43282cfbd", detector.getUuid().toString());
        assertEquals(Collections.emptyList(), detector.getPreDetectionFilters());
        assertEquals(Collections.emptyList(), detector.getPostDetectionFilters());
        assertEquals(AnomalyType.RIGHT_TAILED, params.getType());
        assertEquals(16666.0, thresholds.getUpperStrong(), TOLERANCE);
        assertEquals(2161.0, thresholds.getUpperWeak(), TOLERANCE);
    }

    @Test
    public void testBuildDetectorWithFilters() {
        val factoryUnderTest = new ConstantThresholdDetectorFactoryProvider();
        val document = readDocument("constant-threshold-9amTo5pm-filter");
        val detector = factoryUnderTest.buildDetector(document);

        assertNotNull(detector);
        assertSame(ConstantThresholdDetector.class, detector.getClass());
        assertEquals("42d242d2-42d2-42d2-42d2-42d242d242d2", detector.getUuid().toString());
        assertArrayEquals(expectedPreDetectionFilters().toArray(), detector.getPreDetectionFilters().toArray());
        assertArrayEquals(expectedPostDetectionFilters().toArray(), detector.getPostDetectionFilters().toArray());
    }

    @Test(expected = RuntimeException.class)
    public void testBuildDetector_invalidUuid() {
        readDocument("constant-threshold-invalid-uuid");
    }

    private List<PreDetectionFilter> expectedPreDetectionFilters() {
        return ImmutableList.of(
                new HourOfDayDetectionFilter(9, 17),
                new PassThroughPreDetectionFilter());
    }

    private List<PostDetectionFilter> expectedPostDetectionFilters() {
        return ImmutableList.of(
                new MOfNAggregationFilter(3, 5),
                new PassThroughPostDetectionFilter());
    }

    @Test(expected = RuntimeException.class)
    public void testBuildDetector_invalidParams() {
        val factory = new ConstantThresholdDetectorFactoryProvider();
        val document = readDocument("constant-threshold-invalid-params");
        factory.buildDetector(document);
    }
}
