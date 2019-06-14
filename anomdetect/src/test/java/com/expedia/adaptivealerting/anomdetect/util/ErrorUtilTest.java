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
package com.expedia.adaptivealerting.anomdetect.util;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ErrorUtilTest {

    private static final String DUMMY_MESSAGE = "dummy msg";
    private static final Exception DUMMY_EXCEPTION = new RuntimeException(DUMMY_MESSAGE);

    @Test
    public void testFullExceptionDetails() {
        String res = ErrorUtil.fullExceptionDetails(DUMMY_EXCEPTION);
        assertTrue(res.startsWith(firstTwoLinesStackTrace()));
    }

    @Test
    public void testSingleLineExceptionTrace() {
        String res = ErrorUtil.singleLineExceptionTrace(DUMMY_EXCEPTION);
        assertTrue(res.startsWith("java.lang.RuntimeException: dummy msg (at " + thisClass() + ".<clinit>(" + this.getClass().getSimpleName()));
    }

    @Test
    public void testStackTraceString() {
        String res = ErrorUtil.stackTraceString(DUMMY_EXCEPTION);
        assertTrue(res.startsWith(firstTwoLinesStackTrace()));
    }

    private String firstTwoLinesStackTrace() {
        return "java.lang.RuntimeException: " + DUMMY_MESSAGE + "\n" +
                "\tat " + thisClass();
    }

    private String thisClass() {
        return this.getClass().getName();
    }
}
