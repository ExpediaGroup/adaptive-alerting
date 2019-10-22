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
package com.expedia.adaptivealerting.modelservice.metricsource;

import com.expedia.adaptivealerting.modelservice.metricsource.MetricSourceConfig;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
public class MetricSourceConfigTest extends AbstractJUnit4SpringContextTests {

    @InjectMocks
    private MetricSourceConfig metricSourceConfig;

    @Before
    public void setUp() {
        this.metricSourceConfig = new MetricSourceConfig();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetBean() {
        val serviceTypeName = metricSourceConfig.metricSourceServiceListFactoryBean().getServiceType().getName();
        assertNotNull(serviceTypeName);
        assertEquals("com.expedia.adaptivealerting.modelservice.metricsource.MetricSource", serviceTypeName);
    }
}
