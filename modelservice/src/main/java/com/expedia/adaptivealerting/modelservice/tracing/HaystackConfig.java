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

import com.expedia.www.haystack.client.Tracer;
import com.expedia.www.haystack.client.dispatchers.RemoteDispatcher;
import com.expedia.www.haystack.client.dispatchers.clients.HttpCollectorClient;
import com.expedia.www.haystack.client.idgenerators.RandomUUIDGenerator;
import com.expedia.www.haystack.client.metrics.NoopMetricsRegistry;
import com.expedia.www.haystack.client.propagation.Extractor;
import com.expedia.www.haystack.client.propagation.Injector;
import com.expedia.www.haystack.client.propagation.TextMapPropagator;
import com.expedia.www.haystack.client.propagation.DefaultKeyConvention;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;

// TODO It could be reviewed if a common util like TracingUtil from kafka module
//   be reused with no cyclical dependencies between kafka and modelservice modules.
@Configuration
public class HaystackConfig {

    @Bean
    public Tracer initTracer(HaystackProperties haystackProperties) {
        val collectorHeaders = new HashMap<String, String>();
        collectorHeaders.put("X-Api-Key", haystackProperties.getApiKey());
        collectorHeaders.put("X-Client-Id", haystackProperties.getClientId());
        val metricsRegistry = new NoopMetricsRegistry();

        val dispatcher = new RemoteDispatcher.Builder(
                metricsRegistry,
                new HttpCollectorClient(haystackProperties.getEndpoint(), collectorHeaders)
        ).withBlockingQueueLimit(haystackProperties.getQueueSize())
                .withShutdownTimeoutMillis(haystackProperties.getShutdownInterval())
                .withFlushIntervalMillis(haystackProperties.getFlushInterval())
                .withExecutorThreadCount(haystackProperties.getThreadCount())
                .build();

        val httpPropagator = new TextMapPropagator.Builder()
                .withKeyConvention(new DefaultKeyConvention())
                .withURLCodex()
                .build();

        return new Tracer.Builder(metricsRegistry, haystackProperties.getClientId(), dispatcher)
                .withFormat(
                        Format.Builtin.HTTP_HEADERS, (Extractor<TextMap>) httpPropagator
                ).withFormat(Format.Builtin.HTTP_HEADERS, (Injector<TextMap>) httpPropagator)
                .withDualSpanMode()
                .withIdGenerator(new RandomUUIDGenerator())
                .build();
    }
}
