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
package com.expedia.adaptivealerting.core.datasource;

import com.expedia.www.haystack.commons.entities.MetricPoint;
import com.expedia.www.haystack.commons.entities.MetricType;
import org.junit.Before;
import org.junit.Test;
import scala.Enumeration;
import scala.collection.immutable.Map;
import scala.collection.immutable.Map$;

/**
 * @author Willie Wheeler
 */
public class ConsoleLogDataSourceCallbackTests {
    
    // Class under test
    private ConsoleLogDataSourceCallback callback;
    
    // Test objects
    private MetricPoint metricPoint;
    
    @Before
    public void setUp() {
        this.callback = new ConsoleLogDataSourceCallback();
    
        final Enumeration.Value type = MetricType.Gauge();
        final Map<String, String> tags = Map$.MODULE$.<String, String>empty();
        this.metricPoint = new MetricPoint("myMetric", type, tags, 3.0f, 1000L);
    }
    
    @Test
    public void testNext() {
        callback.next(metricPoint);
    }
}
