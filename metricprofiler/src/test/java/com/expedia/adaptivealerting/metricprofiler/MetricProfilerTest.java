package com.expedia.adaptivealerting.metricprofiler;

import com.expedia.adaptivealerting.metricprofiler.source.ProfileSource;
import com.expedia.metrics.MetricDefinition;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

public class MetricProfilerTest {

    @InjectMocks
    private MetricProfiler metricProfiler;

    @Mock
    private ProfileSource source;

    @Mock
    private Map<String, Boolean> cachedMetrics;

    private MetricDefinition goodDefinition;

    private MatchedMetricResponse metricResponse;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
        this.metricProfiler = new MetricProfiler(source, cachedMetrics);
    }

    @Test
    public void testProfiling() {
        val result = metricProfiler.hasProfilingInfo(goodDefinition);
        assertNotNull(result);
        assertEquals(true, result);
    }

    private void initTestObjects() {
        this.goodDefinition = new MetricDefinition("good-definition");
        this.metricResponse = new MatchedMetricResponse("1", 100L);
    }

    private void initDependencies() {
        when(source.profileExists(Mockito.any(MetricDefinition.class))).thenReturn(metricResponse);
    }
}
