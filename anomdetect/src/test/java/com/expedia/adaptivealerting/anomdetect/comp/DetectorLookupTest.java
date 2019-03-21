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
package com.expedia.adaptivealerting.anomdetect.comp;

import com.expedia.adaptivealerting.anomdetect.detector.CusumDetector;
import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public final class DetectorLookupTest {
    private DetectorLookup lookupUnderTest = new DetectorLookup();

    @Test
    public void testGetDetectorTypes() {
        val types = lookupUnderTest.getDetectorTypes();
        assertNotNull(types);
        assertFalse(types.isEmpty());
    }

    @Test
    public void testLookupCusumDetector() {
        val cusumDetectorClass = lookupUnderTest.getDetector("cusum-detector");
        assertEquals(CusumDetector.class, cusumDetectorClass);
    }

    @Test(expected = RuntimeException.class)
    public void testLookupNonExistentDetector() {
        lookupUnderTest.getDetector("some-nonexistent-detector");
    }
}
