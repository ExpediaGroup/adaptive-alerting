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
package com.expedia.adaptivealerting.kafka.serde;

import com.expedia.metrics.MetricData;
import com.expedia.metrics.jackson.MetricsJavaModule;
import com.fasterxml.jackson.databind.DeserializationFeature;

/**
 * Kafka deserializer to read {@link MetricData}s from JSON.
 *
 * @author Willie Wheeler
 */
public class MetricDataJsonDeserializer extends AbstractJsonDeserializer<MetricData> {
    
    public MetricDataJsonDeserializer() {
        super(MetricData.class);
        getObjectMapper()
                .registerModule(new MetricsJavaModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
