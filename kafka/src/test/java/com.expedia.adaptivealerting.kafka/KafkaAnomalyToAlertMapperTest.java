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
package com.expedia.adaptivealerting.kafka;

import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.TagCollection;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link KafkaAnomalyToAlertMapper}. See
 * https://kafka.apache.org/20/documentation/streams/developer-guide/testing.html
 *
 * @author tbahl
 */
@Slf4j
public class KafkaAnomalyToAlertMapperTest {
    private static final String INBOUND_TOPIC = "anomalies";
    private static final String OUTBOUND_TOPIC = "mdm";

    @Mock
    private StreamsAppConfig streamsAppConfig;

    private MetricData metricData;
    private MappedMetricData mappedMetricData;
    private TopologyTestDriver topologyTestDriver;


    @Before
    public void setUp() {
        this.metricData = TestObjectMother.metricData();
        this.mappedMetricData = TestObjectMother.mappedMetricData(metricData);
        MockitoAnnotations.initMocks(this);
        initConfig();
        initTestTopology();
    }

    @After
    public void tearDown() {
        topologyTestDriver.close();
    }

    @Test
    public void testTransform() {
            assertEquals(getOutputMetricKey(),"some-metric-key");
            assertEquals(getTestObjectValue(), 100.0, 0);
            assertEquals(tagCollection(), getTagMap());
    }

    private void initConfig() {
        when(streamsAppConfig.getInboundTopic()).thenReturn(INBOUND_TOPIC);
        when(streamsAppConfig.getOutboundTopic()).thenReturn(OUTBOUND_TOPIC);
    }

    private void initTestTopology(){
        val topology = new KafkaAnomalyToAlertMapper(streamsAppConfig).buildTopology();
        this.topologyTestDriver = TestObjectMother.topologyTestDriver(topology, MappedMetricData.class, false);
    }

    private TagCollection tagCollection(){
        val tags = new HashMap<String, String>();
        tags.put("mtype", "gauge");
        tags.put("unit", "");
        tags.put("interval","1");
        tags.put("org_id","1");
        return new TagCollection(tags);
    }

    private String getOutputMetricKey() {
        return mappedMetricData.getMetricData().getMetricDefinition().getKey();
    }

    private TagCollection getTagMap() {
        return (TagCollection) mappedMetricData.getMetricData().getMetricDefinition().getTags();
    }

    private double getTestObjectValue(){
        return metricData.getValue();
    }
}