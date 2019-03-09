/*
 * Copyright 2018-2019 Expedia Group, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expedia.adaptivealerting.tools.pipeline.sink;

import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.adaptivealerting.tools.visualization.ChartSeries;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import lombok.val;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.title.TextTitle;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.when;

public class AnomalyChartSinkTest {
    private AnomalyChartSink sinkUnderTest;
    
    @Mock
    private JFreeChart chart;
    
    @Mock
    private TextTitle textTitle;
    
    private ChartSeries chartSeries;
    private MappedMetricData anomaly;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
        this.sinkUnderTest = new AnomalyChartSink(chart, chartSeries);
    }
    
    @Test
    public void testNext() {
        sinkUnderTest.next(anomaly);
    }
    
    private void initTestObjects() {
        this.chartSeries = new ChartSeries();
    
        val metricDef = new MetricDefinition("my-metric");
        val metricData = new MetricData(metricDef, 1.0, Instant.now().getEpochSecond());
        val anomalyResult = new AnomalyResult();
        
        this.anomaly = new MappedMetricData(metricData, UUID.randomUUID(), "ewma-detector");
        anomaly.setAnomalyResult(anomalyResult);
    }
    
    private void initDependencies() {
        when(textTitle.getText()).thenReturn("My Chart");
        when(chart.getTitle()).thenReturn(textTitle);
    }
}
