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
package com.expedia.adaptivealerting.anomdetect.holtwinters;

import org.junit.Test;

import static org.junit.Assert.*;

public class SeasonalityTypeParserTest {
    private SeasonalityTypeParser subject = new SeasonalityTypeParser();

    @Test
    public void parse() {
        assertEquals(SeasonalityType.ADDITIVE, subject.parse("ADDITIVE"));
        assertEquals(SeasonalityType.ADDITIVE, subject.parse("additive"));
        assertEquals(SeasonalityType.MULTIPLICATIVE, subject.parse("MULTIPLICATIVE"));
        assertEquals(SeasonalityType.MULTIPLICATIVE, subject.parse("multiplicative"));
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidTextInput() {
        subject.parse("bad_seasonality");
    }
}