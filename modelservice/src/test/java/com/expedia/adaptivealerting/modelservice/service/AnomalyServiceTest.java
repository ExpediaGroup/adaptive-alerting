package com.expedia.adaptivealerting.modelservice.service;

import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.modelservice.entity.Metric;
import com.expedia.adaptivealerting.modelservice.graphite.GraphiteResult;
import com.expedia.adaptivealerting.modelservice.graphite.GraphiteTemplate;
import com.expedia.adaptivealerting.modelservice.graphite.Tags;
import com.expedia.adaptivealerting.modelservice.repo.MetricRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Slf4j
public class AnomalyServiceTest {

    @InjectMocks
    private AnomalyService anomalyService;

    @Mock
    private GraphiteTemplate graphiteTemplate;

    @Mock
    private MetricRepository metricRepository;

    private Metric metric;

    private GraphiteResult[] results;

    @Before
    public void setUp() {
        this.anomalyService = new AnomalyService();
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
    }

    @Test
    public void testGetAnomalies() {
        AnomalyRequest request = getAnomalyRequest();
        List<ModifiedAnomalyResult> actualResults = anomalyService.getAnomalies(request);
        assertNotNull(actualResults);
        assertEquals(AnomalyLevel.WEAK, actualResults.get(0).getLevel());
        assertEquals(AnomalyLevel.STRONG, actualResults.get(1).getLevel());
    }

    private void initTestObjects() {
        this.metric = getMetric();
        this.results = new GraphiteResult[2];
        results[0] = getGraphiteData();
    }

    private void initDependencies() {
        when(metricRepository.findByHash(anyString())).thenReturn(metric);
        when(graphiteTemplate.getMetricData(anyString())).thenReturn(results);
    }

    private AnomalyRequest getAnomalyRequest() {
        AnomalyRequest request = new AnomalyRequest();
        Map detectorParams = new HashMap<String, Object>();
        detectorParams.put("upperStrong", 80.0);
        detectorParams.put("upperWeak", 70.0);
        detectorParams.put("type", "RIGHT_TAILED");

        request.setDetectorParams(detectorParams);
        request.setDetectorType("constant-detector");
        request.setHash("1.3dec7f4218c57c1839147f8ca190ed55");
        return request;
    }

    private Metric getMetric() {
        Metric metric = new Metric();
        metric.setHash("1.3dec7f4218c57c1839147f8ca190ed55");
        metric.setKey("karmalab.stats.gauges.AirBoss.chelappabo004_karmalab_net.java.nio.BufferPool.mapped.TotalCapacity30");
        return metric;
    }

    private GraphiteResult getGraphiteData() {
        GraphiteResult result = new GraphiteResult();
        String[][] datapoints = new String[2][2];
        datapoints[0][0] = "78.0";
        datapoints[0][1] = "1548829800";
        datapoints[1][0] = "81.0";
        datapoints[1][1] = "1548830400";
        result.setDatapoints(datapoints);
        result.setTags(new Tags());
        result.setTarget("karmalab.stats.gauges.AirBoss.chelappabo004_karmalab_net.java.nio.BufferPool.mapped.TotalCapacity30");
        return result;
    }

}
