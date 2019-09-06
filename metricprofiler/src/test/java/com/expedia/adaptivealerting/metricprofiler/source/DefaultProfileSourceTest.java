package com.expedia.adaptivealerting.metricprofiler.source;

import com.expedia.metrics.MetricDefinition;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

public class DefaultProfileSourceTest {

    private DefaultProfileSource sourceUnderTest;

    @Mock
    private MetricProfilerClient client;

    private MetricDefinition goodDefinition;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
        this.sourceUnderTest = new DefaultProfileSource(client);
    }

    @Test
    public void testProfilingExists() {
        val result = sourceUnderTest.profileExists(goodDefinition);
        assertSame(true, result);
    }

    private void initTestObjects() {
        this.goodDefinition = new MetricDefinition("good-definition");
    }

    private void initDependencies() {
        when(client.profileExists(Mockito.any(MetricDefinition.class))).thenReturn(true);
    }

}
