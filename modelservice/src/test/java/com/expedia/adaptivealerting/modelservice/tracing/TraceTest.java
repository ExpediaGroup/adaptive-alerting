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

import com.expedia.www.haystack.client.SpanContext;
import com.expedia.www.haystack.client.Tracer;
import com.expedia.www.haystack.client.dispatchers.NoopDispatcher;
import com.expedia.www.haystack.client.metrics.NoopMetricsRegistry;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;
import lombok.val;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.UUID;
import com.expedia.www.haystack.client.dispatchers.Dispatcher;
import com.expedia.www.haystack.client.metrics.MetricsRegistry;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class TraceTest {

    private Trace utilUnderTest;

    private Tracer tracer = Mockito.mock(Tracer.class);

    private Tracer noOpsTracer;

    private UUID traceId;

    private UUID spanId;

    private UUID parentSpanId;

    private Dispatcher dispatcher;

    private MetricsRegistry metrics;

    private SpanContext testSpanContext;

    @Before
    public void setUp() {
        initTestObjects();
        metrics = new NoopMetricsRegistry();
        dispatcher = new NoopDispatcher();
        noOpsTracer = new Tracer.Builder(metrics, "testTrace", dispatcher).build();
        utilUnderTest = new Trace(tracer);
    }

    @Test
    public void testNullSpanContext() throws NullPointerException {
        val testChildSpan = noOpsTracer.buildSpan("testOperationName");
        SpanContext testNullSpanContext = null;
        when(tracer.buildSpan("testOperationName")).thenReturn(testChildSpan);
        val resultSpan = utilUnderTest.startSpan("testOperationName", testNullSpanContext);
        Assert.assertEquals("testOperationName", resultSpan.getOperationName());
    }

    @Test
    public void testNonNullSpanContext() {
        val testChildSpanContext = noOpsTracer.buildSpan("testChildOperationName").asChildOf(testSpanContext);
        when(tracer.buildSpan("testChildOperationName")).thenReturn(testChildSpanContext);
        val resultChildSpan = utilUnderTest.startSpan("testChildOperationName", testSpanContext);
        Assert.assertEquals(testSpanContext.toTraceId(), resultChildSpan.context().toTraceId());
        Assert.assertEquals(testSpanContext.toSpanId(), resultChildSpan.context().getParentId().toString());

    }

    @Test
    public void testExtractSpanContext() {
        HttpHeaders testHttpHeaders = new HttpHeaders();
        testHttpHeaders.add("test-header-key", "test-header-value");
        final HashMap<String, String> mapHeaders = new HashMap<>();
        testHttpHeaders.forEach((key, value) -> {
            mapHeaders.put(key, value.get(0));
        });
        when(tracer.extract(eq(Format.Builtin.HTTP_HEADERS), ArgumentMatchers.any(TextMapAdapter.class))).thenReturn(testSpanContext);
        io.opentracing.SpanContext resultSpanContext = utilUnderTest.extractParentSpan(testHttpHeaders);
        Assert.assertEquals(resultSpanContext.toTraceId(), traceId.toString());
        Assert.assertEquals(resultSpanContext.toSpanId(), spanId.toString());
    }

    private void initTestObjects() {
        this.traceId = UUID.randomUUID();
        this.spanId = UUID.randomUUID();
        this.parentSpanId = UUID.randomUUID();
        this.testSpanContext = new SpanContext(traceId, spanId, parentSpanId);
    }

}
