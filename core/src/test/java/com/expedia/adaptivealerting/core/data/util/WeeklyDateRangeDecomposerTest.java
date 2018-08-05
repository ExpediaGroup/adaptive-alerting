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
import com.expedia.adaptivealerting.core.util.WeeklyDateRangeDecomposer;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Willie Wheeler
 */
public final class WeeklyDateRangeDecomposerTest extends AbstractDateRangeDecomposerTest {
    private WeeklyDateRangeDecomposer decomposer;
    
    @Override
    protected DateRangeDecomposer getDecomposer() {
        return decomposer;
    }
    
    @Before
    public void setUp() {
        this.decomposer = new WeeklyDateRangeDecomposer();
    }
    
    @Test
    public void testDecompose_0w_cleanBounds() {
        doTestDecompose(
                "2018-04-02T00:00:00Z",
                "2018-04-03T00:00:00Z",
                0,
                null,
                null);
    }
    
    @Test
    public void testDecompose_0w_dirtyBounds() {
        doTestDecompose(
                "2018-04-02T14:00:00Z",
                "2018-04-03T14:00:00Z",
                0,
                null,
                null);
    }
    
    @Test
    public void testDecompose_1w_cleanBounds() {
        doTestDecompose(
                "2018-04-01T00:00:00Z",
                "2018-04-08T00:00:00Z",
                1,
                "2018-04-01T00:00:00Z",
                "2018-04-01T00:00:00Z");
    }
    
    @Test
    public void testDecompose_1w_dirtyBounds() {
        doTestDecompose(
                "2018-04-02T00:00:00Z",
                "2018-04-08T00:00:00Z",
                1,
                "2018-04-01T00:00:00Z",
                "2018-04-01T00:00:00Z");
    }
    
    @Test
    public void testDecompose_2w_cleanBounds() {
        doTestDecompose(
                "2018-04-01T00:00:00Z",
                "2018-04-15T00:00:00Z",
                2,
                "2018-04-01T00:00:00Z",
                "2018-04-08T00:00:00Z");
    }
    
    @Test
    public void testDecompose_2w_dirtyBounds() {
        doTestDecompose(
                "2018-04-02T03:45:15Z",
                "2018-04-16T03:45:15Z",
                2,
                "2018-04-01T00:00:00Z",
                "2018-04-08T00:00:00Z");
    }
}
