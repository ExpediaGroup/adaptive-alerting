package com.expedia.adaptivealerting.modelservice.service;

import com.expedia.adaptivealerting.anomdetect.outlier.AnomalyLevel;
import com.expedia.adaptivealerting.modelservice.providers.graphite.GraphiteMetricSource;
import com.expedia.adaptivealerting.modelservice.spi.MetricSource;
import com.expedia.adaptivealerting.modelservice.spi.MetricSourceResult;
import com.expedia.adaptivealerting.modelservice.test.ObjectMother;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class AnomalyServiceTest {

    @InjectMocks
    private AnomalyService serviceUnderTest = new AnomalyServiceImpl();

    @Spy
    @Qualifier("metricSourceServiceListFactoryBean")
    private List<MetricSource> metricSources = new ArrayList<>();

    @Mock
    private GraphiteMetricSource graphiteMetricSource;

    @Spy
    private MetricSourceResult metricSourceResult;

    @Spy
    private List<MetricSourceResult> metricSourceResults = new ArrayList<>();

    private AnomalyRequest anomalyRequest;

    @Before
    public void setUp() {
        initTestObjects();
        initDependencies();
        metricSources.add(graphiteMetricSource);
        metricSourceResults.add(metricSourceResult);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetAnomalies() {
        val actualResults = serviceUnderTest.getAnomalies(anomalyRequest);
        assertNotNull(actualResults);
        assertEquals(1, actualResults.size());
        assertEquals(AnomalyLevel.WEAK, actualResults.get(0).getAnomalyLevel());
        verify(graphiteMetricSource, atMost(1)).getMetricData(anyString());
    }

    private void initTestObjects() {
        val mom = ObjectMother.instance();
        anomalyRequest = mom.getAnomalyRequest();
        metricSourceResult = mom.getMetricData();
    }

    private void initDependencies() {
        when(graphiteMetricSource.getMetricData(anyString())).thenReturn(metricSourceResults);
    }
}
