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

import com.expedia.www.haystack.client.Span;
import com.expedia.www.haystack.client.Tracer;
import io.opentracing.SpanContext;
import io.opentracing.propagation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Slf4j
@Service
public class Trace {

    private final Tracer modelServiceApiTracer;

    @Autowired
    public Trace(Tracer tracer){
        this.modelServiceApiTracer = tracer;
    }

    public Span startSpan(String operationName, SpanContext spnContext){
        Span span;
        if (spnContext!= null){
            span = modelServiceApiTracer.buildSpan(operationName).asChildOf(spnContext).start();
        }
        else {
            span = modelServiceApiTracer.buildSpan(operationName).start();
        }
        return span;
    }

    public SpanContext extractParentSpan(HttpHeaders headers){
        final HashMap<String, String> mapHeaders = new HashMap<>();
        headers.forEach((key, value) -> {
            mapHeaders.put(key, value.get(0));
        });
        log.info("mapHeaders {}", mapHeaders);

        try {
            SpanContext parentSpan = modelServiceApiTracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapAdapter(mapHeaders));
            log.info("parentSpan {}", parentSpan);
            return parentSpan;
        } catch (IllegalArgumentException e) {
            log.error("Exception during extracting parentSpan", e);
        }
        return null;

    }
}
