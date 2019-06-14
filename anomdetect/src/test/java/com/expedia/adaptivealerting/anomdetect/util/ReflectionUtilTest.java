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

import com.expedia.adaptivealerting.anomdetect.outlier.AnomalyResult;
import com.expedia.adaptivealerting.anomdetect.outlier.AnomalyThresholds;
import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ReflectionUtilTest {

    @Test
    public void testNewInstance() {
        val result = ReflectionUtil.newInstance(AnomalyResult.class);
        assertEquals(AnomalyResult.class, result.getClass());
    }

    @Test(expected = RuntimeException.class)
    public void testNewInstance_instantiationException() {
        // This generates an InstantiationException because AnomalyThresholds lacks a noarg constructor.
        ReflectionUtil.newInstance(AnomalyThresholds.class);
    }
}
