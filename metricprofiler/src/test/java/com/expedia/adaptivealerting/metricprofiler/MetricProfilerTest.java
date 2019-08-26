package com.expedia.adaptivealerting.metricprofiler;

import com.expedia.adaptivealerting.metricprofiler.source.ProfilingSource;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

public class MetricProfilerTest {

    @InjectMocks
    private MetricProfiler metricProfiler;

    @Mock
    private ProfilingSource source;

    @Mock
    private Map<String, Boolean> cachedMetrics;

    private MetricData goodMetricData;
    private MetricDefinition goodDefinition;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
        this.metricProfiler = new MetricProfiler(source, cachedMetrics);
    }

    @Test
    public void testProfiling() {
        val result = metricProfiler.hasProfilingInfo(goodMetricData);
        assertNotNull(result);
        assertEquals(true, result);
    }

    private void initTestObjects() {
        this.goodDefinition = new MetricDefinition("good-definition");
        this.goodMetricData = new MetricData(goodDefinition, 100.0, Instant.now().getEpochSecond());
    }

    private void initDependencies() {
        when(source.profilingExists(Mockito.any(Map.class))).thenReturn(true);
    }
}
