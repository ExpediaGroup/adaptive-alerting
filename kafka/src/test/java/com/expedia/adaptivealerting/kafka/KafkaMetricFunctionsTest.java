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

import com.github.charithe.kafka.EphemeralKafkaBroker;
import com.github.charithe.kafka.KafkaJunitRule;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

@Slf4j
public class KafkaMetricFunctionsTest {

    // Metric producer

    @ClassRule
    public static KafkaJunitRule kafka = new KafkaJunitRule(EphemeralKafkaBroker.create()).waitForStartup();

    public KafkaMetricFunctions functionsUnderTest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.functionsUnderTest = new KafkaMetricFunctions();
    }

    // FIXME This is generating an error related to not seeing the proper config. Not sure what's up yet. [WLW]
    @Test
    @Ignore
    public void testInitPublisher() {
        functionsUnderTest.initPublisher();
    }
}
