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
package com.expedia.adaptivealerting.anomdetect.detector.aggregator;

import com.expedia.adaptivealerting.anomdetect.detector.aggregator.config.MOfNAggregatorConfig;
import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.anomaly.AnomalyThresholds;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class MOfNAggregatorTest {
    private AnomalyResult normalResult;
    private AnomalyResult weakResult;
    private AnomalyResult strongResult;
    private AnomalyResult warmupResult;

    @Before
    public void setUp() {
        this.normalResult = new AnomalyResult()
                .setAnomalyLevel(AnomalyLevel.NORMAL)
                .setPredicted(42.2)
                .setThresholds(new AnomalyThresholds(100.0, 90.0, 20.0, 10.0));
        this.weakResult = new AnomalyResult()
                .setAnomalyLevel(AnomalyLevel.WEAK)
                .setPredicted(95.1)
                .setThresholds(new AnomalyThresholds(102.0, 92.0, 22.0, 12.0));
        this.strongResult = new AnomalyResult()
                .setAnomalyLevel(AnomalyLevel.STRONG)
                .setPredicted(105.8)
                .setThresholds(new AnomalyThresholds(101.0, 91.0, 21.0, 11.0));
        this.warmupResult = new AnomalyResult()
                .setAnomalyLevel(AnomalyLevel.MODEL_WARMUP)
                .setPredicted(37.2)
                .setThresholds(new AnomalyThresholds(103.0, 93.0, 23.0, 13.0));
    }

    @Test
    public void testConstructor() {
        val config = new MOfNAggregatorConfig(4, 6);
        val aggregator = new MOfNAggregator(config);
        assertEquals(config, aggregator.getConfig());
    }

    @Test
    public void testAggregate_singleNormal() {
        val aggregator = new MOfNAggregator();
        val aggregatedResult = aggregator.aggregate(normalResult);
        assertEquals(AnomalyLevel.NORMAL, aggregatedResult.getAnomalyLevel());
        assertEquals(normalResult.getPredicted(), aggregatedResult.getPredicted());
        assertEquals(normalResult.getThresholds(), aggregatedResult.getThresholds());
    }

    @Test
    public void testAggregate_singleWeak() {
        val aggregator = new MOfNAggregator();
        val aggregatedResult = aggregator.aggregate(weakResult);
        assertEquals(AnomalyLevel.WEAK, aggregatedResult.getAnomalyLevel());
        assertEquals(weakResult.getPredicted(), aggregatedResult.getPredicted());
        assertEquals(weakResult.getThresholds(), aggregatedResult.getThresholds());
    }

    @Test
    public void testAggregate_mConsecutiveWeaks() {
        val aggregator = new MOfNAggregator();

        val inputResult = weakResult;
        AnomalyResult outputResult;

        outputResult = aggregator.aggregate(inputResult);
        assertEquals(inputResult.getAnomalyLevel(), outputResult.getAnomalyLevel());

        outputResult = aggregator.aggregate(inputResult);
        assertEquals(inputResult.getAnomalyLevel(), outputResult.getAnomalyLevel());

        outputResult = aggregator.aggregate(inputResult);
        assertEquals(AnomalyLevel.STRONG, outputResult.getAnomalyLevel());
    }

    @Test
    public void testAggregate_mNonConsecutiveWeaks() {
        val aggregator = new MOfNAggregator();

        AnomalyResult inputResult;
        AnomalyResult outputResult;

        inputResult = weakResult;
        outputResult = aggregator.aggregate(inputResult);
        assertEquals(inputResult.getAnomalyLevel(), outputResult.getAnomalyLevel());

        inputResult = normalResult;
        outputResult = aggregator.aggregate(inputResult);
        assertEquals(inputResult.getAnomalyLevel(), outputResult.getAnomalyLevel());

        inputResult = weakResult;
        outputResult = aggregator.aggregate(inputResult);
        assertEquals(inputResult.getAnomalyLevel(), outputResult.getAnomalyLevel());

        inputResult = normalResult;
        outputResult = aggregator.aggregate(inputResult);
        assertEquals(inputResult.getAnomalyLevel(), outputResult.getAnomalyLevel());

        inputResult = weakResult;
        outputResult = aggregator.aggregate(inputResult);
        assertEquals(AnomalyLevel.STRONG, outputResult.getAnomalyLevel());
    }

    @Test
    public void testAggregate_mConsecutiveMixedAnomalies() {
        val aggregator = new MOfNAggregator();

        AnomalyResult inputResult;
        AnomalyResult outputResult;

        inputResult = weakResult;
        outputResult = aggregator.aggregate(inputResult);
        assertEquals(inputResult.getAnomalyLevel(), outputResult.getAnomalyLevel());

        inputResult = strongResult;
        outputResult = aggregator.aggregate(inputResult);
        assertEquals(inputResult.getAnomalyLevel(), outputResult.getAnomalyLevel());

        inputResult = weakResult;
        outputResult = aggregator.aggregate(inputResult);
        assertEquals(AnomalyLevel.STRONG, outputResult.getAnomalyLevel());
    }

    @Test
    public void testAggregate_nWarmupsThenMixedLevels() {
        val aggregator = new MOfNAggregator();

        AnomalyResult inputResult = warmupResult;
        AnomalyResult outputResult;

        outputResult = aggregator.aggregate(inputResult);
        assertEquals(inputResult.getAnomalyLevel(), outputResult.getAnomalyLevel());

        outputResult = aggregator.aggregate(inputResult);
        assertEquals(inputResult.getAnomalyLevel(), outputResult.getAnomalyLevel());

        outputResult = aggregator.aggregate(inputResult);
        assertEquals(inputResult.getAnomalyLevel(), outputResult.getAnomalyLevel());

        outputResult = aggregator.aggregate(inputResult);
        assertEquals(inputResult.getAnomalyLevel(), outputResult.getAnomalyLevel());

        outputResult = aggregator.aggregate(inputResult);
        assertEquals(inputResult.getAnomalyLevel(), outputResult.getAnomalyLevel());

        inputResult = weakResult;
        outputResult = aggregator.aggregate(inputResult);
        assertEquals(inputResult.getAnomalyLevel(), outputResult.getAnomalyLevel());

        inputResult = strongResult;
        outputResult = aggregator.aggregate(inputResult);
        assertEquals(inputResult.getAnomalyLevel(), outputResult.getAnomalyLevel());

        inputResult = normalResult;
        outputResult = aggregator.aggregate(inputResult);
        assertEquals(inputResult.getAnomalyLevel(), outputResult.getAnomalyLevel());

        inputResult = weakResult;
        outputResult = aggregator.aggregate(inputResult);
        assertEquals(AnomalyLevel.STRONG, outputResult.getAnomalyLevel());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAggregate_nullAnomalyResult() {
        new MOfNAggregator().aggregate(null);
    }
}
