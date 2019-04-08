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

import com.expedia.adaptivealerting.anomdetect.forecast.ForecastingDetector;
import com.expedia.adaptivealerting.anomdetect.forecast.interval.ExponentialWelfordIntervalForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.point.EwmaPointForecaster;
import com.expedia.adaptivealerting.core.anomaly.AnomalyType;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.adaptivealerting.tools.pipeline.util.AnomalyResultSubscriber;
import com.expedia.adaptivealerting.tools.util.TestObjectMother;
import com.expedia.metrics.MetricData;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

public final class DetectorFilterTest {
    private DetectorFilter filterUnderTest;
    private MetricData metricData;

    @Before
    public void setUp() {
        val detector = new ForecastingDetector(
                UUID.randomUUID(),
                new EwmaPointForecaster(),
                new ExponentialWelfordIntervalForecaster(),
                AnomalyType.TWO_TAILED);
        this.filterUnderTest = new DetectorFilter(detector);

        val metricDef = TestObjectMother.metricDefinition();
        this.metricData = new MetricData(metricDef, 10.0, Instant.now().getEpochSecond());
    }

    @Test
    public void testNext() {
        boolean[] gotNext = new boolean[1];

        val subscriber = new AnomalyResultSubscriber() {
            @Override
            public void next(MappedMetricData anomaly) {
                gotNext[0] = true;
            }
        };

        filterUnderTest.addSubscriber(subscriber);
        filterUnderTest.next(metricData);
        filterUnderTest.removeSubscriber(subscriber);

        assertTrue(gotNext[0]);
    }
}
