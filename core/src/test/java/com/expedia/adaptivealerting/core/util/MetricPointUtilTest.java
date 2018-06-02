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

import com.expedia.adaptivealerting.core.OutlierLevel;
import com.expedia.www.haystack.commons.entities.MetricPoint;
import org.junit.Test;

import java.time.Instant;

import static com.expedia.adaptivealerting.core.util.MetricPointUtil.OUTLIER_LEVEL_TAG_NAME;
import static com.expedia.adaptivealerting.core.util.MetricPointUtil.classify;
import static com.expedia.adaptivealerting.core.util.MetricPointUtil.metricPoint;
import static org.junit.Assert.assertEquals;

public class MetricPointUtilTest {
    
    @Test
    public void testMetricPoint() {
        final MetricPoint actual = metricPoint(Instant.now(), 1.414f);
        assertEquals("data", actual.metric());
    }
    
    @Test
    public void testClassify() {
        final MetricPoint orig = metricPoint(Instant.now(), 1.414f);
        final MetricPoint copy = classify(orig, OutlierLevel.STRONG);
        assertEquals("data", copy.metric());
        assertEquals(OutlierLevel.STRONG.name(), copy.tags().get(OUTLIER_LEVEL_TAG_NAME).get());
    }
}
