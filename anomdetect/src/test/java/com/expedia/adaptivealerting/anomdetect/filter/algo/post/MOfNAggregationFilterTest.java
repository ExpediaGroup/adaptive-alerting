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
package com.expedia.adaptivealerting.anomdetect.filter.algo.post;

import com.expedia.adaptivealerting.anomdetect.detect.AnomalyLevel;
import com.expedia.adaptivealerting.anomdetect.detect.AnomalyThresholds;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorResult;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.OutlierDetectorResult;
import com.expedia.adaptivealerting.anomdetect.filter.chain.PostDetectionFilterChain;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@RunWith(MockitoJUnitRunner.class)
public final class MOfNAggregationFilterTest {
    private OutlierDetectorResult normalResult;
    private OutlierDetectorResult weakResult;
    private OutlierDetectorResult strongResult;
    private OutlierDetectorResult warmupResult;
    @Mock
    private DetectorResult mockNonOutlierResult;

    @Before
    public void setUp() {
        this.normalResult = new OutlierDetectorResult()
                .setAnomalyLevel(AnomalyLevel.NORMAL)
                .setPredicted(42.2)
                .setThresholds(new AnomalyThresholds(100.0, 90.0, 20.0, 10.0))
                .setTrusted(true);
        this.weakResult = new OutlierDetectorResult()
                .setAnomalyLevel(AnomalyLevel.WEAK)
                .setPredicted(95.1)
                .setThresholds(new AnomalyThresholds(102.0, 92.0, 22.0, 12.0))
                .setTrusted(true);
        this.strongResult = new OutlierDetectorResult()
                .setAnomalyLevel(AnomalyLevel.STRONG)
                .setPredicted(105.8)
                .setThresholds(new AnomalyThresholds(101.0, 91.0, 21.0, 11.0))
                .setTrusted(true);
        this.warmupResult = new OutlierDetectorResult()
                .setAnomalyLevel(AnomalyLevel.MODEL_WARMUP)
                .setPredicted(37.2)
                .setThresholds(new AnomalyThresholds(103.0, 93.0, 23.0, 13.0))
                .setTrusted(true);
    }

    @Test
    public void testConstructor() {
        val aggregator = new MOfNAggregationFilter(4, 6);
        assertEquals(4, aggregator.getM());
        assertEquals(6, aggregator.getN());
    }

    @Test
    public void testAggregate_singleNormal() {
        val aggregator = new MOfNAggregationFilter(3, 5);
        val aggregatedResult = ((OutlierDetectorResult) aggregator.doFilter(normalResult, noopChain()));
        assertEquals(AnomalyLevel.NORMAL, aggregatedResult.getAnomalyLevel());
        assertEquals(normalResult.getPredicted(), aggregatedResult.getPredicted());
        assertEquals(normalResult.getThresholds(), aggregatedResult.getThresholds());
        assertEquals(normalResult.isTrusted(), aggregatedResult.isTrusted());
    }

    @Test
    public void testAggregate_singleWeak() {
        val aggregator = new MOfNAggregationFilter(3, 5);
        OutlierDetectorResult aggregatedResult = (OutlierDetectorResult) aggregator.doFilter(weakResult, noopChain());
        assertEquals(AnomalyLevel.WEAK, aggregatedResult.getAnomalyLevel());
        assertEquals(weakResult.getPredicted(), aggregatedResult.getPredicted());
        assertEquals(weakResult.getThresholds(), aggregatedResult.getThresholds());
        assertEquals(normalResult.isTrusted(), aggregatedResult.isTrusted());
    }

    @Test
    public void testAggregate_mConsecutiveWeaks() {
        val aggregator = new MOfNAggregationFilter(3, 5);

        val inputResult = weakResult;
        OutlierDetectorResult outputResult;

        outputResult = (OutlierDetectorResult) aggregator.doFilter(inputResult, noopChain());
        assertEquals(inputResult.getAnomalyLevel(), outputResult.getAnomalyLevel());

        outputResult = (OutlierDetectorResult) aggregator.doFilter(inputResult, noopChain());
        assertEquals(inputResult.getAnomalyLevel(), outputResult.getAnomalyLevel());

        outputResult = (OutlierDetectorResult) aggregator.doFilter(inputResult, noopChain());
        assertEquals(AnomalyLevel.STRONG, outputResult.getAnomalyLevel());
    }

    @Test
    public void testAggregate_mNonConsecutiveWeaks() {
        val aggregator = new MOfNAggregationFilter(3, 5);

        OutlierDetectorResult inputResult;
        OutlierDetectorResult outputResult;

        inputResult = weakResult;
        outputResult = (OutlierDetectorResult) aggregator.doFilter(inputResult, noopChain());
        assertEquals(inputResult.getAnomalyLevel(), outputResult.getAnomalyLevel());

        inputResult = normalResult;
        outputResult = (OutlierDetectorResult) aggregator.doFilter(inputResult, noopChain());
        assertEquals(inputResult.getAnomalyLevel(), outputResult.getAnomalyLevel());

        inputResult = weakResult;
        outputResult = (OutlierDetectorResult) aggregator.doFilter(inputResult, noopChain());
        assertEquals(inputResult.getAnomalyLevel(), outputResult.getAnomalyLevel());

        inputResult = normalResult;
        outputResult = (OutlierDetectorResult) aggregator.doFilter(inputResult, noopChain());
        assertEquals(inputResult.getAnomalyLevel(), outputResult.getAnomalyLevel());

        inputResult = weakResult;
        outputResult = (OutlierDetectorResult) aggregator.doFilter(inputResult, noopChain());
        assertEquals(AnomalyLevel.STRONG, outputResult.getAnomalyLevel());
    }

    @Test
    public void testAggregate_mConsecutiveMixedAnomalies() {
        val aggregator = new MOfNAggregationFilter(3, 5);

        OutlierDetectorResult inputResult;
        OutlierDetectorResult outputResult;

        inputResult = weakResult;
        outputResult = (OutlierDetectorResult) aggregator.doFilter(inputResult, noopChain());
        assertEquals(inputResult.getAnomalyLevel(), outputResult.getAnomalyLevel());

        inputResult = strongResult;
        outputResult = (OutlierDetectorResult) aggregator.doFilter(inputResult, noopChain());
        assertEquals(inputResult.getAnomalyLevel(), outputResult.getAnomalyLevel());

        inputResult = weakResult;
        outputResult = (OutlierDetectorResult) aggregator.doFilter(inputResult, noopChain());
        assertEquals(AnomalyLevel.STRONG, outputResult.getAnomalyLevel());
    }

    @Test
    public void testAggregate_nWarmupsThenMixedLevels() {
        val aggregator = new MOfNAggregationFilter(3, 5);

        OutlierDetectorResult inputResult = warmupResult;
        OutlierDetectorResult outputResult;

        outputResult = (OutlierDetectorResult) aggregator.doFilter(inputResult, noopChain());
        assertEquals(inputResult.getAnomalyLevel(), outputResult.getAnomalyLevel());

        outputResult = (OutlierDetectorResult) aggregator.doFilter(inputResult, noopChain());
        assertEquals(inputResult.getAnomalyLevel(), outputResult.getAnomalyLevel());

        outputResult = (OutlierDetectorResult) aggregator.doFilter(inputResult, noopChain());
        assertEquals(inputResult.getAnomalyLevel(), outputResult.getAnomalyLevel());

        outputResult = (OutlierDetectorResult) aggregator.doFilter(inputResult, noopChain());
        assertEquals(inputResult.getAnomalyLevel(), outputResult.getAnomalyLevel());

        outputResult = (OutlierDetectorResult) aggregator.doFilter(inputResult, noopChain());
        assertEquals(inputResult.getAnomalyLevel(), outputResult.getAnomalyLevel());

        inputResult = weakResult;
        outputResult = (OutlierDetectorResult) aggregator.doFilter(inputResult, noopChain());
        assertEquals(inputResult.getAnomalyLevel(), outputResult.getAnomalyLevel());

        inputResult = strongResult;
        outputResult = (OutlierDetectorResult) aggregator.doFilter(inputResult, noopChain());
        assertEquals(inputResult.getAnomalyLevel(), outputResult.getAnomalyLevel());

        inputResult = normalResult;
        outputResult = (OutlierDetectorResult) aggregator.doFilter(inputResult, noopChain());
        assertEquals(inputResult.getAnomalyLevel(), outputResult.getAnomalyLevel());

        inputResult = weakResult;
        outputResult = (OutlierDetectorResult) aggregator.doFilter(inputResult, noopChain());
        assertEquals(AnomalyLevel.STRONG, outputResult.getAnomalyLevel());
    }

    @Test
    public void testAggregate_nonOutlierResult() {
        DetectorResult result = new MOfNAggregationFilter(3, 5).doFilter(mockNonOutlierResult, noopChain());
        assertSame(mockNonOutlierResult, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidate_mZero() {
        new MOfNAggregationFilter(0, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidate_nGreaterThanM() {
        new MOfNAggregationFilter(5, 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAggregate_nullAnomalyResult() {
        new MOfNAggregationFilter(3, 5).doFilter(null, noopChain());
    }

    private static PostDetectionFilterChain noopChain() {
        return new PostDetectionFilterChain(emptyList());
    }
}
