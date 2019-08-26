package com.expedia.adaptivealerting.metricprofiler.source;

import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

public class DefaultProfilingSourceTest {

    private DefaultProfilingSource sourceUnderTest;

    @Mock
    private ProfilingClient client;

    private Map<String, String> metricTags;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
        this.sourceUnderTest = new DefaultProfilingSource(client);
    }

    @Test
    public void testProfilingExists() {
        val result = sourceUnderTest.profilingExists(metricTags);
        assertSame(true, result);
    }

    private void initTestObjects() {
        this.metricTags = new HashMap<>();
    }

    private void initDependencies() {
        when(client.findProfilingDocument(Mockito.any(Map.class))).thenReturn(true);
    }

}
