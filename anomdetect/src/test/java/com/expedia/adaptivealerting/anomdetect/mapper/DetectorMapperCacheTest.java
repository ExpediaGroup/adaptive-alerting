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
package com.expedia.adaptivealerting.anomdetect.mapper;

import com.codahale.metrics.MetricRegistry;
import com.google.common.cache.Cache;
import lombok.val;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class DetectorMapperCacheTest {

    @Mock
    private Cache<String, String> cache;

    @InjectMocks
    private DetectorMapperCache detectorMapperCache = new DetectorMapperCache(new MetricRegistry());


    private String detectorIds;
    private List<Detector> detectors;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void get() {
        val key = "metricKeyWithValue";
        val key2 = "metricKeyWithOutValue";
        detectors = Collections.singletonList(new Detector("", UUID.randomUUID()));
        detectorIds = CacheUtil.getValue(detectors);

        Mockito.when(cache.getIfPresent(key)).thenReturn(detectorIds);

        Assert.assertEquals(detectors, detectorMapperCache.get(key));
        verify(cache, times(1)).getIfPresent(key);

        Assert.assertEquals(Collections.emptyList(), detectorMapperCache.get(key2));
        verify(cache, times(1)).getIfPresent(key);

    }

    @Test
    public void put() {
        detectors = Collections.singletonList(new Detector("", UUID.randomUUID()));

        detectorMapperCache.put("key", detectors);
        detectorIds = CacheUtil.getValue(detectors);
        verify(cache, times(1)).put("key", detectorIds);
    }
}
