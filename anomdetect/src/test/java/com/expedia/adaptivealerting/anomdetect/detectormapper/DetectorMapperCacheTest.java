package com.expedia.adaptivealerting.anomdetect.detectormapper;

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

    @Mock
    private MetricRegistry metricRegistry = new MetricRegistry();

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
        detectors = Collections.singletonList(new Detector(UUID.randomUUID()));
        detectorIds = CacheUtil.getDetectorIds(detectors);

        Mockito.when(cache.getIfPresent(key)).thenReturn(detectorIds);

        Assert.assertEquals(detectors, detectorMapperCache.get(key));
        verify(cache, times(1)).getIfPresent(key);

        Assert.assertEquals(Collections.emptyList(), detectorMapperCache.get(key2));
        verify(cache, times(1)).getIfPresent(key);

    }

    @Test
    public void put() {
        detectors = Collections.singletonList(new Detector(UUID.randomUUID()));

        detectorMapperCache.put("key", detectors);
        detectorIds = CacheUtil.getDetectorIds(detectors);
        verify(cache, times(1)).put("key", detectorIds);
    }
}
