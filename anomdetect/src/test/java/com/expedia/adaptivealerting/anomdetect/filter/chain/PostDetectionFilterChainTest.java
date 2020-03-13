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
package com.expedia.adaptivealerting.anomdetect.filter.chain;

import com.expedia.adaptivealerting.anomdetect.detect.DetectorResult;
import com.expedia.adaptivealerting.anomdetect.filter.PostDetectionFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PostDetectionFilterChainTest {
    private PostDetectionFilterChain chainUnderTest;

    @Mock
    private PostDetectionFilter preDetectionFilter;
    @Mock
    private DetectorResult detectorResult;

    @Before
    public void setUp() {
    }

    @Test
    public void doFilterWithEmptyChain() {
        chainUnderTest = new PostDetectionFilterChain(Collections.emptyList());
        chainUnderTest.doFilter(detectorResult);
    }

    @Test
    public void doFilterWithChain() {
        chainUnderTest = new PostDetectionFilterChain(singletonList(preDetectionFilter));
        when(preDetectionFilter.doFilter(detectorResult, chainUnderTest)).thenReturn(detectorResult);
        DetectorResult result = chainUnderTest.doFilter(detectorResult);
        assertSame(detectorResult, result);
    }
}