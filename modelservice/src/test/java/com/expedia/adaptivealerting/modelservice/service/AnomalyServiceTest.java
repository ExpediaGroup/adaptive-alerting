package com.expedia.adaptivealerting.modelservice.service;

import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.modelservice.providers.graphite.GraphiteMetricSource;
import com.expedia.adaptivealerting.modelservice.spi.MetricSource;
import com.expedia.adaptivealerting.modelservice.spi.MetricSourceResult;
import com.expedia.adaptivealerting.modelservice.test.ObjectMother;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.verification.VerificationMode;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.*;

import static org.junit.Assert.*;

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

    @Mock
    private MetricSourceResult metricSourceResult = new MetricSourceResult();

    @Spy
    private List<MetricSourceResult> metricSourceResults = new ArrayList<>();

    private AnomalyRequest anomalyRequest;

    @Before
    public void setUp() {
        metricSources.add(graphiteMetricSource);
        metricSourceResults.add(metricSourceResult);
        MockitoAnnotations.initMocks(this);
        initTestObjects();
    }

    @Test
    public void testGetAnomalies() {
        mockSource(graphiteMetricSource);
        List<AnomalyResult> actualResults = anomalyService.getAnomalies(anomalyRequest);
        verifyNumberOfSourceCalls(graphiteMetricSource, Mockito.atMost(1));
        assertNotNull(actualResults);
        Assert.assertEquals(0, actualResults.size());
    }

    private void initTestObjects() {
        ObjectMother mom = ObjectMother.instance();
        anomalyRequest = mom.getAnomalyRequest();
    }

    private void mockSource(MetricSource source) {
        System.out.println(metricSourceResults.size());
        Mockito.when(source.getMetricData(Mockito.anyString())).thenReturn(metricSourceResults);
    }

    private void verifyNumberOfSourceCalls(MetricSource metricSource, VerificationMode verifMode) {
        Mockito.verify(metricSource, verifMode).getMetricData(Mockito.anyString());
    }
}