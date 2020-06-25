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
package com.expedia.adaptivealerting.modelservice.tracing;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

public class HaystackConfigTest {

    @InjectMocks
    private HaystackConfig haystackConfig;

    @Mock
    private HaystackProperties haystackProperties;

    @Before
    public void setUp() {
        this.haystackConfig = new HaystackConfig();
        MockitoAnnotations.initMocks(this);
        initTestObjects();
    }

    @Test
    public void testHaystackConfig() {
        assertNotNull(haystackConfig.initTracer(haystackProperties));
    }

    private void initTestObjects() {
        this.haystackProperties = new HaystackProperties();
        haystackProperties.setApiKey("abcdefghijklmnopqrstuvwxyz");
        haystackProperties.setClientId("modelserviceclientid");
        haystackProperties.setEndpoint("relativehaystackcollectorendpoint");
        haystackProperties.setFlushInterval(15000);
        haystackProperties.setQueueSize(3000);
        haystackProperties.setShutdownInterval(15000);
        haystackProperties.setThreadCount(24);
    }
}
