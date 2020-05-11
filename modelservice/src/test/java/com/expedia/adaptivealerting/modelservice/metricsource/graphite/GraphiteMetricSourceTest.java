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
package com.expedia.adaptivealerting.modelservice.metricsource.graphite;

import com.expedia.adaptivealerting.modelservice.metricsource.MetricSourceResult;
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
import java.util.HashMap;
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
                buildGraphiteData());
        when(BeanUtil.getBean(GraphiteProperties.class)).thenReturn(props);
    }

    private GraphiteResult[] buildGraphiteData() {
        GraphiteResult[] results = new GraphiteResult[1];
        GraphiteResult result = new GraphiteResult();
        result.setDatapoints(getDataPoints());
        result.setTags(getTags());
        result.setTarget("target");
        results[0] = result;
        return results;
    }

    private String[][] getDataPoints() {
        String[][] datapoints = new String[2][2];
        datapoints[0][0] = String.valueOf(78.0);
        datapoints[0][1] = String.valueOf(1548829800);
        datapoints[1][0] = String.valueOf(81.0);
        datapoints[1][1] = String.valueOf(1548830400);
        return datapoints;
    }

    private Map<String, Object> getTags() {
        Map<String, Object> tags = new HashMap<String, Object>();
        tags.put("lob", "hotel");
        tags.put("pos", "expedia-com");
        return tags;
    }
}
