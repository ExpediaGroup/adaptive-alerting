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

import com.expedia.adaptivealerting.metricprofiler.MetricProfiler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.charithe.kafka.EphemeralKafkaBroker;
import com.github.charithe.kafka.KafkaJunitRule;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

@Slf4j
public class KafkaMetricProfilerTest {

    // Consumes from metrics topic and sends metrics to profiled to profile-metrics kafka topic.
    private static final String INPUT_TOPIC = "metrics";
    private static final String OUTPUT_TOPIC = "profile-metrics";

    @ClassRule
    public static KafkaJunitRule kafka = new KafkaJunitRule(EphemeralKafkaBroker.create()).waitForStartup();

    private KafkaMetricProfiler managerUnderTest;
    private ObjectMapper objectMapper;

    @Mock
    private MetricProfiler metricProfiler;

    @Mock
    private StreamsAppConfig saConfig;

    @Mock
    private Config tsConfig;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initConfig();
    }

    @Test
    public void testBuildMetricProfiler() {
        this.managerUnderTest = new KafkaMetricProfiler(saConfig, metricProfiler);
        val topology = managerUnderTest.buildTopology();
    }

    private void initConfig() {
        when(saConfig.getTypesafeConfig()).thenReturn(tsConfig);
        when(saConfig.getInputTopic()).thenReturn(INPUT_TOPIC);
        when(saConfig.getOutputTopic()).thenReturn(OUTPUT_TOPIC);
    }

}
