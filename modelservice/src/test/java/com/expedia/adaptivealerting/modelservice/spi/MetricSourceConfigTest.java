package com.expedia.adaptivealerting.modelservice.spi;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
public class MetricSourceConfigTest extends AbstractJUnit4SpringContextTests {

    @InjectMocks
    private MetricSourceConfig metricSourceConfig;

    @Before
    public void setUp() {
        this.metricSourceConfig = new MetricSourceConfig();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetBean() {
        val serviceTypeName = metricSourceConfig.metricSourceServiceListFactoryBean().getServiceType().getName();
        assertNotNull(serviceTypeName);
        assertEquals("com.expedia.adaptivealerting.modelservice.spi.MetricSource", serviceTypeName);
    }
}
