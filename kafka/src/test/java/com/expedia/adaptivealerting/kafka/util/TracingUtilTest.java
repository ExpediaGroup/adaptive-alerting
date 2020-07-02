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
package com.expedia.adaptivealerting.kafka.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.val;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TracingUtilTest {

    private static final String TRACING_APIKEY_STRING = "apiKey";
    private static final String TRACING_CLIENTID_STRING = "clientId";
    private static final String TRACING_ENDPOINT_STRING = "endpoint";
    private static final String TRACING_QUEUESIZE_STRING = "queueSize";
    private static final String TRACING_SHUTDOWNTIMEOUT_STRING = "shutdownTimeout";
    private static final String TRACING_FLUSHINTERVAL_STRING = "flushInterval";
    private static final String TRACING_THREADCOUNT_STRING = "threadCount";
    private static final String CK_TRACING = "tracing";

    private Config config;
    private Config tracingConfig;

    @Before
    public void setUp(){
        config = ConfigFactory.load("detector-manager.conf");
        tracingConfig = config.getConfig(CK_TRACING);
    }

    @Test
    public void testTracingConfig() {
        val config = ConfigFactory.load("detector-manager.conf");
        val tracingConfig = config.getConfig(CK_TRACING);
        assertEquals("acdefghijklmnopqrstuvwxyz", tracingConfig.getString(TRACING_APIKEY_STRING));
        assertEquals("ad-manager", tracingConfig.getString(TRACING_CLIENTID_STRING));
        assertEquals("https://localhost/span", tracingConfig.getString(TRACING_ENDPOINT_STRING));
        assertEquals(3000, tracingConfig.getInt(TRACING_QUEUESIZE_STRING));
        assertEquals(15000, tracingConfig.getInt(TRACING_FLUSHINTERVAL_STRING));
        assertEquals(15000, tracingConfig.getInt(TRACING_SHUTDOWNTIMEOUT_STRING));
        assertEquals(24, tracingConfig.getInt(TRACING_THREADCOUNT_STRING));
    }

    @Test
    public void testGetTracer() {
        HashMap<String, String> collectorHeaders = new HashMap<>();
        val tracer = TracingUtil.getTracer(collectorHeaders, tracingConfig);
        Assert.assertNotNull(tracer);
    }

}
