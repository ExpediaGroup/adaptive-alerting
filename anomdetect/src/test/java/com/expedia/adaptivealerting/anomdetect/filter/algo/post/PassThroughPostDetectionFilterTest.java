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
package com.expedia.adaptivealerting.anomdetect.filter.algo.post;

import com.expedia.adaptivealerting.anomdetect.detect.outlier.OutlierDetectorResult;
import com.expedia.adaptivealerting.anomdetect.filter.chain.PostDetectionFilterChain;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PassThroughPostDetectionFilterTest {
    private PassThroughPostDetectionFilter filterUnderTest = new PassThroughPostDetectionFilter();
    private OutlierDetectorResult outlierDetectorResult = new OutlierDetectorResult();
    @Mock
    private PostDetectionFilterChain mockChain;

    @Test
    public void testFilteredWithChain() {
        when(mockChain.doFilter(outlierDetectorResult)).thenReturn(outlierDetectorResult);
        val filteredResult = filterUnderTest.doFilter(outlierDetectorResult, mockChain);
        assertSame(outlierDetectorResult, filteredResult);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFilter_nullAnomalyResult() {
        filterUnderTest.doFilter(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFilter_nullChain() {
        filterUnderTest.doFilter(outlierDetectorResult, null);
    }

}

