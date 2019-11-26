package com.expedia.adaptivealerting.modelservice.acutator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MetricFilterTest {

    @Spy
    @InjectMocks
    private MetricFilter metricFilter;

    @Mock
    private CustomActuatorMetricService actuatorMetricService;

    private FilterConfig config;
    private HttpServletRequest req;
    private HttpServletResponse resp;
    private FilterChain next;

    @Before
    public void setUp() {
        config = Mockito.mock(FilterConfig.class);
        req = Mockito.mock(HttpServletRequest.class);
        resp = Mockito.mock(HttpServletResponse.class);
        next = Mockito.mock(FilterChain.class);
    }

    @Test
    public void testMetricFilter() throws Exception {
        metricFilter.init(config);
        metricFilter.doFilter(req, resp, next);
        metricFilter.destroy();
        verify(metricFilter, times(1)).init(config);
        verify(metricFilter, times(1)).doFilter(req, resp, next);
        verify(metricFilter, times(1)).destroy();
    }

}
