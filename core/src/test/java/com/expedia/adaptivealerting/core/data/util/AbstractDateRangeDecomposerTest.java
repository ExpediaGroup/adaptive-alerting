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
package com.expedia.adaptivealerting.core.data.util;

import com.expedia.adaptivealerting.core.util.DateRangeDecomposer;

import java.time.Instant;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Willie Wheeler
 */
public abstract class AbstractDateRangeDecomposerTest {
    
    protected abstract DateRangeDecomposer getDecomposer();
    
    protected void doTestDecompose(
            String startDateStr,
            String endDateStr,
            int expectedSize,
            String expectedFirstDateStr,
            String expectedLastDateStr) {
        
        final Instant startDate = Instant.parse(startDateStr);
        final Instant endDate = Instant.parse(endDateStr);
        final List<Instant> result = getDecomposer().decompose(startDate, endDate);
        assertEquals(expectedSize, result.size());
        if (expectedFirstDateStr != null) {
            assertEquals(Instant.parse(expectedFirstDateStr), result.get(0));
            assertEquals(Instant.parse(expectedLastDateStr), result.get(result.size() - 1));
        }
    }
}
