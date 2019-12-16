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
package com.expedia.adaptivealerting.modelservice.exception;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Generated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unchecked", "rawtypes"})
@Slf4j
@ControllerAdvice
@Generated
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    private Counter exceptionCounter;

    @Autowired
    public CustomExceptionHandler(MeterRegistry meterRegistry) {
        this.exceptionCounter = meterRegistry.counter("exceptionCounter");
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Object> handleAllExceptions(Exception ex) {
        List<String> details = new ArrayList<>();
        details.add(ex.getLocalizedMessage());
        ErrorResponse error = new ErrorResponse("Server Error", details);
        log.error("Exception occurred "+ error);
        exceptionCounter.increment();
        return new ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {RecordNotFoundException.class})
    protected ResponseEntity<Object> handleRecordNotFound(final RuntimeException ex, final WebRequest request) {
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Object> handleIllegalArgument(final RuntimeException ex, final WebRequest request) {
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

}