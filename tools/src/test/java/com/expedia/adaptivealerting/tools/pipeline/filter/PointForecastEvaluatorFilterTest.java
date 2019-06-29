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
package com.expedia.adaptivealerting.tools.pipeline.filter;

import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;
import com.expedia.adaptivealerting.anomdetect.detect.AnomalyResult;
import com.expedia.adaptivealerting.anomdetect.forecast.PointForecastEvaluation;
import com.expedia.adaptivealerting.anomdetect.forecast.RmsePointForecastEvaluator;
import com.expedia.adaptivealerting.tools.pipeline.util.ModelEvaluationSubscriber;
import com.expedia.adaptivealerting.tools.util.TestObjectMother;
import com.expedia.metrics.MetricData;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

public final class PointForecastEvaluatorFilterTest {
    private EvaluatorFilter filterUnderTest;
    private MappedMetricData classified;

    @Before
    public void setUp() {
        val evaluator = new RmsePointForecastEvaluator();
        this.filterUnderTest = new EvaluatorFilter(evaluator);

        val metricDef = TestObjectMother.metricDefinition();
        val metricData = new MetricData(metricDef, 10.0, Instant.now().getEpochSecond());

        val anomalyResult = new AnomalyResult();

        this.classified = new MappedMetricData(metricData, UUID.randomUUID());
        classified.setAnomalyResult(anomalyResult);
    }

    @Test
    public void testNext() {
        boolean[] gotNext = new boolean[1];

        val subscriber = new ModelEvaluationSubscriber() {
            @Override
            public void next(PointForecastEvaluation evaluation) {
                gotNext[0] = true;
            }
        };

        filterUnderTest.addSubscriber(subscriber);
        filterUnderTest.next(classified);
        filterUnderTest.removeSubscriber(subscriber);

        assertTrue(gotNext[0]);
    }
}
