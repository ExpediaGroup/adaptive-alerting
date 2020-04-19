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
package com.expedia.adaptivealerting.anomdetect.filter.algo;

import com.expedia.adaptivealerting.anomdetect.detect.AnomalyLevel;
import com.expedia.adaptivealerting.anomdetect.detect.AnomalyThresholds;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorRequest;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorResponse;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorResult;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.OutlierDetectorResult;
import com.expedia.adaptivealerting.anomdetect.filter.chain.DetectionFilterChain;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.expedia.adaptivealerting.anomdetect.detect.AnomalyLevel.MODEL_WARMUP;
import static com.expedia.adaptivealerting.anomdetect.detect.AnomalyLevel.NORMAL;
import static com.expedia.adaptivealerting.anomdetect.detect.AnomalyLevel.STRONG;
import static com.expedia.adaptivealerting.anomdetect.detect.AnomalyLevel.WEAK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public final class MOfNAggregationFilterTest {
    public static final boolean TRUSTED = true;
    private OutlierDetectorResult normalResult;
    private OutlierDetectorResult weakResult;
    private OutlierDetectorResult strongResult;
    private OutlierDetectorResult warmupResult;
    @Mock
    private DetectorRequest detectorRequest;
    @Mock
    private DetectorResult mockNonOutlierResult;
    @Mock
    private DetectionFilterChain mockFilterChain;

    @Before
    public void setUp() {
        this.normalResult = new OutlierDetectorResult()
                .setAnomalyLevel(NORMAL)
                .setPredicted(42.2)
                .setThresholds(new AnomalyThresholds(100.0, 90.0, 20.0, 10.0))
                .setTrusted(TRUSTED);
        this.weakResult = new OutlierDetectorResult()
                .setAnomalyLevel(WEAK)
                .setPredicted(95.1)
                .setThresholds(new AnomalyThresholds(102.0, 92.0, 22.0, 12.0))
                .setTrusted(TRUSTED);
        this.strongResult = new OutlierDetectorResult()
                .setAnomalyLevel(STRONG)
                .setPredicted(105.8)
                .setThresholds(new AnomalyThresholds(101.0, 91.0, 21.0, 11.0))
                .setTrusted(TRUSTED);
        this.warmupResult = new OutlierDetectorResult()
                .setAnomalyLevel(AnomalyLevel.MODEL_WARMUP)
                .setPredicted(37.2)
                .setThresholds(new AnomalyThresholds(103.0, 93.0, 23.0, 13.0))
                .setTrusted(TRUSTED);
    }

    @Test
    public void testConstructor() {
        val aggregator = new MOfNAggregationFilter(4, 6);
        assertEquals(4, aggregator.getM());
        assertEquals(6, aggregator.getN());
    }

    @Test
    public void testAggregate_singleNormal() {
        checkFilterResult(new MOfNAggregationFilter(3, 5), normalResult, NORMAL);
    }

    @Test
    public void testAggregate_singleWeak() {
        checkFilterResult(new MOfNAggregationFilter(3, 5), weakResult, WEAK);
    }

    @Test
    public void testAggregate_mConsecutiveWeaks() {
        val aggregator = new MOfNAggregationFilter(3, 5);

        checkFilterResult(aggregator, weakResult, WEAK);
        checkFilterResult(aggregator, weakResult, WEAK);
        checkFilterResult(aggregator, weakResult, STRONG);
    }

    @Test
    public void testAggregate_mNonConsecutiveWeaks() {
        val aggregator = new MOfNAggregationFilter(3, 5);

        checkFilterResult(aggregator, weakResult, WEAK);
        checkFilterResult(aggregator, normalResult, NORMAL);
        checkFilterResult(aggregator, weakResult, WEAK);
        checkFilterResult(aggregator, normalResult, NORMAL);
        checkFilterResult(aggregator, weakResult, STRONG);
    }

    @Test
    public void testAggregate_mConsecutiveMixedAnomalies() {
        val aggregator = new MOfNAggregationFilter(3, 5);

        checkFilterResult(aggregator, weakResult, WEAK);
        checkFilterResult(aggregator, strongResult, STRONG);
        checkFilterResult(aggregator, weakResult, STRONG);
    }

    @Test
    public void testAggregate_nWarmupsThenMixedLevels() {
        val aggregator = new MOfNAggregationFilter(3, 5);

        checkFilterResult(aggregator, warmupResult, MODEL_WARMUP);
        checkFilterResult(aggregator, warmupResult, MODEL_WARMUP);
        checkFilterResult(aggregator, warmupResult, MODEL_WARMUP);
        checkFilterResult(aggregator, warmupResult, MODEL_WARMUP);
        checkFilterResult(aggregator, warmupResult, MODEL_WARMUP);

        checkFilterResult(aggregator, weakResult, WEAK);
        checkFilterResult(aggregator, strongResult, STRONG);
        checkFilterResult(aggregator, normalResult, NORMAL);
        checkFilterResult(aggregator, weakResult, STRONG);
    }

    @Test
    public void testAggregate_ignoresNonOutlierResult() {
        val nonOutlierResponse = detectorResponse(mockNonOutlierResult);
        new MOfNAggregationFilter(3, 5).doFilter(detectorRequest, nonOutlierResponse, mockFilterChain);
        assertSame(mockNonOutlierResult, nonOutlierResponse.getDetectorResult());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidate_mZero() {
        new MOfNAggregationFilter(0, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidate_nGreaterThanM() {
        new MOfNAggregationFilter(5, 3);
    }

    @Test
    public void testAggregate_nullResponseArgument() {
        try {
            new MOfNAggregationFilter(3, 5).doFilter(detectorRequest, null, mockFilterChain);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException e) {
            assertEquals("detectorResponse is marked non-null but is null", e.getMessage());
        }
    }

    private DetectorResponse detectorResponse(DetectorResult result) {
        DetectorResponse response = new DetectorResponse();
        response.setDetectorResult(result);
        return response;
    }

    private void checkFilterResult(MOfNAggregationFilter aggregator, OutlierDetectorResult inputResult, AnomalyLevel expectedAnomalyLevel) {
        DetectorResponse inputResponse = detectorResponse(inputResult);
        aggregator.doFilter(detectorRequest, inputResponse, mockFilterChain);
        OutlierDetectorResult aggregatedResult = (OutlierDetectorResult) inputResponse.getDetectorResult();
        assertEquals(expectedAnomalyLevel, aggregatedResult.getAnomalyLevel());
        assertEquals(inputResult.getPredicted(), aggregatedResult.getPredicted());
        assertEquals(inputResult.getThresholds(), aggregatedResult.getThresholds());
        assertEquals(inputResult.isTrusted(), aggregatedResult.isTrusted());
    }

}
