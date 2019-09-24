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
package com.expedia.adaptivealerting.metrics.functions;

import com.expedia.adaptivealerting.kafka.TypesafeConfigLoader;
import com.expedia.adaptivealerting.kafka.KafkaMetricFunctions;
import com.expedia.adaptivealerting.metrics.functions.source.MetricFunctionsReader;
import com.expedia.adaptivealerting.metrics.functions.source.MetricFunctionsSpec;
import com.expedia.metrics.MetricData;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.clients.producer.Producer;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MetricFunctionsApp {

    private static final String APP_ID = "aa-metric-functions";
    private static final String METRIC_SOURCE_SINK = "metric-source-sink";
    private static final String INPUT_FUNCTIONS_FILENAME ="functions.txt";
    private final static String INPUT_FILE_PATH = "/config/";
    private static final int corePoolSize = 5;

    public static void main(String[] args) {
        val config = new TypesafeConfigLoader(APP_ID).loadMergedConfig();
        val metricSourceSinkConfig = config.getConfig(METRIC_SOURCE_SINK);
        KafkaMetricFunctions kafkaMetricFunctions = new KafkaMetricFunctions();
        Producer<String, MetricData> aggregatorProducer = kafkaMetricFunctions.getMetricFunctionsSink(config);
        val input_file = INPUT_FILE_PATH + INPUT_FUNCTIONS_FILENAME;
        List<MetricFunctionsSpec> metricFunctionSpecs = MetricFunctionsReader.readFromInputFile(input_file);
        if (metricFunctionSpecs.isEmpty()) {
            log.error("Error with input functions file, exiting..." );
        }
        ScheduledExecutorService execService
                = Executors.newScheduledThreadPool(corePoolSize);
        for (MetricFunctionsSpec metricFunctionSpec: metricFunctionSpecs) {
            MetricFunctionsTask metricFunctionsTask = new MetricFunctionsTask(metricFunctionSpec,
                    aggregatorProducer,
                    metricSourceSinkConfig);
            execService.scheduleAtFixedRate(metricFunctionsTask,
                    0, metricFunctionSpec.getIntervalInSecs(), TimeUnit.SECONDS);
        }

    }

}

