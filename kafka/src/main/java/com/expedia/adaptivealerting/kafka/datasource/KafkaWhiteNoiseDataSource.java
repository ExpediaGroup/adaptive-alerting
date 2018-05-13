/*
 * Copyright 2018 Expedia Group, Inc.
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
package com.expedia.adaptivealerting.kafka.datasource;

import com.expedia.adaptivealerting.core.metricsource.WhiteNoiseMetricSource;

/**
 * <p>
 * Kafka producer that generates an infinite series of white noise {@link com.expedia.www.haystack.commons.entities.MetricPoint}s
 * at a given rate. The data distribution is Gaussian and configurable. The rate is likewise configurable.
 * </p>
 * <p>
 * This class is primarily intended to support testing.
 * </p>
 *
 * @author Willie Wheeler
 */
public class KafkaWhiteNoiseDataSource {
    
    public static void main(String[] args) {
        new WhiteNoiseMetricSource().start(new KafkaMetricSourceCallback("metrics"));
    }
}
