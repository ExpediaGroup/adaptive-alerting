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

import com.expedia.www.haystack.client.Tracer;
import com.expedia.www.haystack.client.dispatchers.RemoteDispatcher;
import com.expedia.www.haystack.client.dispatchers.clients.HttpCollectorClient;
import com.expedia.www.haystack.client.idgenerators.RandomUUIDGenerator;
import com.expedia.www.haystack.client.metrics.NoopMetricsRegistry;
import com.expedia.www.haystack.client.propagation.DefaultKeyConvention;
import com.expedia.www.haystack.client.propagation.Extractor;
import com.expedia.www.haystack.client.propagation.Injector;
import com.expedia.www.haystack.client.propagation.TextMapPropagator;
import com.typesafe.config.Config;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import lombok.val;

import java.util.HashMap;

public class TracingUtil {
    private static final String TRACER_HEADER_APIKEY_STRING = "X-Api-Key";
    private static final String TRACER_HEADER_CLIENTID_STRING = "X-Client-Id";
    private static final String TRACING_APIKEY_STRING = "apiKey";
    private static final String TRACING_CLIENTID_STRING = "clientId";
    private static final String TRACING_ENDPOINT_STRING = "endpoint";
    private static final String TRACING_QUEUESIZE_STRING = "queueSize";
    private static final String TRACING_SHUTDOWNTIMEOUT_STRING = "shutdownTimeout";
    private static final String TRACING_FLUSHINTERVAL_STRING = "flushInterval";
    private static final String TRACING_THREADCOUNT_STRING = "threadCount";

    public static Tracer getTracer(HashMap<String, String> collectorHeaders, Config tracingConfig) {
        collectorHeaders.put(TRACER_HEADER_APIKEY_STRING, tracingConfig.getString(TRACING_APIKEY_STRING));
        collectorHeaders.put(TRACER_HEADER_CLIENTID_STRING, tracingConfig.getString(TRACING_CLIENTID_STRING));
        val metricsRegistry = new NoopMetricsRegistry();
        val dispatcher = createRemoteDispatcher(metricsRegistry, collectorHeaders, tracingConfig);
        val textMapPropagator = createTextMapPropagator();

        return new Tracer.Builder(metricsRegistry, tracingConfig.getString("clientId"), dispatcher)
                .withFormat(
                        Format.Builtin.TEXT_MAP, (Extractor<TextMap>) textMapPropagator
                ).withFormat(Format.Builtin.TEXT_MAP, (Injector<TextMap>) textMapPropagator)
                .withDualSpanMode()
                .withIdGenerator(new RandomUUIDGenerator())
                .build();
    }

    private static RemoteDispatcher createRemoteDispatcher(NoopMetricsRegistry metricsRegistry,
                                                           HashMap<String, String> collectorHeaders,
                                                           Config tracingConfig){
        return new RemoteDispatcher.Builder(
                metricsRegistry,
                new HttpCollectorClient(tracingConfig.getString(TRACING_ENDPOINT_STRING), collectorHeaders)
        ).withBlockingQueueLimit(tracingConfig.getInt(TRACING_QUEUESIZE_STRING))
                .withShutdownTimeoutMillis(tracingConfig.getLong(TRACING_SHUTDOWNTIMEOUT_STRING))
                .withFlushIntervalMillis(tracingConfig.getLong(TRACING_FLUSHINTERVAL_STRING))
                .withExecutorThreadCount(tracingConfig.getInt(TRACING_THREADCOUNT_STRING))
                .build();
    }

    private static TextMapPropagator createTextMapPropagator() {
        return new TextMapPropagator.Builder()
                .withKeyConvention(new DefaultKeyConvention())
                .withURLCodex()
                .build();
    }
}

