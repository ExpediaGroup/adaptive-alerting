package com.expedia.adaptivealerting.modelservice.service;

import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.modelservice.entity.Metric;
import com.expedia.adaptivealerting.modelservice.providers.graphite.GraphiteMetricSource;
import com.expedia.adaptivealerting.modelservice.providers.graphite.GraphiteProperties;
import com.expedia.adaptivealerting.modelservice.providers.graphite.GraphiteResult;
import com.expedia.adaptivealerting.modelservice.repo.MetricRepository;
import com.expedia.adaptivealerting.modelservice.spi.MetricSource;
import com.expedia.adaptivealerting.modelservice.spi.MetricSourceResult;
import com.expedia.adaptivealerting.modelservice.test.ObjectMother;
import com.expedia.adaptivealerting.modelservice.util.BeanUtil;
import com.expedia.adaptivealerting.modelservice.util.DetectorUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class AnomalyServiceTest {

    @InjectMocks
    private AnomalyService anomalyService = new AnomalyServiceImpl();

    @Spy
    @Qualifier("metricSourceServiceListFactoryBean")
    private List<MetricSource> metricSources = new ArrayList<>();

    @Mock
    private GraphiteMetricSource graphiteMetricSource;

    @Spy
    private List<MetricSourceResult> results = new ArrayList<>();

    private AnomalyRequest anomalyRequest;

    @Before
    public void setUp() {
        metricSources.add(graphiteMetricSource);
        this.results.add(new MetricSourceResult());
        MockitoAnnotations.initMocks(this);
        initTestObjects();
    }

    @Test
    public void testGetAnomalies() {
        List<AnomalyResult> actualResults = anomalyService.getAnomalies(anomalyRequest);
        assertNotNull(actualResults);
    }

    private void initTestObjects() {
        ObjectMother mom = ObjectMother.instance();
        anomalyRequest = mom.getAnomalyRequest();
    }
}