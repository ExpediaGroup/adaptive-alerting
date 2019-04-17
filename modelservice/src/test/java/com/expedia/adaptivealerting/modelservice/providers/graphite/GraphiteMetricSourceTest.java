package com.expedia.adaptivealerting.modelservice.providers.graphite;

import com.expedia.adaptivealerting.modelservice.spi.MetricSourceResult;
import com.expedia.adaptivealerting.modelservice.test.ObjectMother;
import com.expedia.adaptivealerting.modelservice.util.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class GraphiteMetricSourceTest {

    @InjectMocks
    private GraphiteMetricSource graphiteMetricSource;

    @Mock
    private BeanUtil beanUtil;

    @Mock
    private ApplicationContext ctx;

    @Mock
    private RestTemplate restTemplate;

    private List<MetricSourceResult> results = new ArrayList<>();

    @Before
    public void setUp() {
        this.graphiteMetricSource = new GraphiteMetricSource();
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
    }

    @Test
    public void testGetMetricData() {
        List<MetricSourceResult> metricSourceResults = graphiteMetricSource.getMetricData("metricKey");
        assertNotNull(metricSourceResults);
        assertEquals(2, metricSourceResults.size());
    }

    private void initTestObjects() {
        this.beanUtil = new BeanUtil();
        beanUtil.setApplicationContext(ctx);

        ObjectMother mom = ObjectMother.instance();
        results.add(mom.getMetricData());
    }

    private void initDependencies() {
        ObjectMother mom = ObjectMother.instance();
        GraphiteProperties props = new GraphiteProperties();
        props.setUrlTemplate("https://graphiteUrl/render?from=-24hours&format=json&maxDataPoints=144&target={target}");
        when(restTemplate.getForObject(anyString(), eq(GraphiteResult[].class), any(Map.class))).thenReturn(
                mom.getGraphiteData());
        when(BeanUtil.getBean(GraphiteProperties.class)).thenReturn(props);
    }
}
