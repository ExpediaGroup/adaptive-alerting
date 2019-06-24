package com.expedia.adaptivealerting.modelservice.util;

import com.expedia.adaptivealerting.modelservice.plugin.graphite.GraphiteProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {GraphiteProperties.class})
public class BeanUtilTest extends AbstractJUnit4SpringContextTests {

    @InjectMocks
    private BeanUtil beanUtil;

    @Autowired
    private ApplicationContext ctx;

    @Before
    public void setUp() {
        this.beanUtil = new BeanUtil();
        beanUtil.setApplicationContext(ctx);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetBean() {
        GraphiteProperties classBean = BeanUtil.getBean(GraphiteProperties.class);
        assertNotNull(classBean);
    }
}
