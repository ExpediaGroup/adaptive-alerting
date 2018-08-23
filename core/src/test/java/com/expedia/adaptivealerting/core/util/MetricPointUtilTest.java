/*
 * Copyright 2018 Expedia Group, Inc.
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
package com.expedia.adaptivealerting.core.util;

import com.expedia.adaptivealerting.core.data.Mpoint;
import com.expedia.www.haystack.commons.entities.MetricPoint;
import com.expedia.www.haystack.commons.entities.MetricType;
import com.expedia.www.haystack.commons.entities.TagKeys;
import org.junit.Test;
import scala.Predef;
import scala.Predef$;
import scala.Tuple2;
import scala.collection.immutable.Map;
import scala.collection.mutable.WrappedArray;

import java.time.Instant;

import static com.expedia.adaptivealerting.core.util.MetricUtil.metricPoint;
import static com.expedia.adaptivealerting.core.util.MetricUtil.toMpoint;
import static org.junit.Assert.assertEquals;

public class MetricPointUtilTest {
    
    @Test
    public void testMetricPoint() {
        final MetricPoint actual = metricPoint(Instant.now().getEpochSecond(), 1.414f);
        assertEquals("data", actual.metric());
    }

    @Test
    public void testToMpoint() {
        final Tuple2[] ts = { new Tuple2(TagKeys.SERVICE_NAME_KEY(), "expweb"),
            new Tuple2(TagKeys.OPERATION_NAME_KEY(), "service:GPS")};
        final WrappedArray wa = Predef.wrapRefArray(ts);
        final Map<String, String> tags = Predef$.MODULE$.Map().apply(wa);
        MetricPoint metricPoint = new MetricPoint("data", MetricType.Count(), tags, 10,
            Instant.now().getEpochSecond());
        Mpoint mpoint = toMpoint(metricPoint);
        assertEquals(10, mpoint.getValue(), 0.001);
        assertEquals("expweb", mpoint.getMetricDefinition().getTag(TagKeys.SERVICE_NAME_KEY()));
        assertEquals("service:GPS", mpoint.getMetricDefinition().getTag(TagKeys.OPERATION_NAME_KEY()));
    }
}
