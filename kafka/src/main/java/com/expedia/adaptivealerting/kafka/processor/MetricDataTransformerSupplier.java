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
package com.expedia.adaptivealerting.kafka.processor;

import com.expedia.adaptivealerting.anomdetect.mapper.DetectorMapper;
import com.expedia.adaptivealerting.anomdetect.mapper.MapperResult;
import com.expedia.metrics.MetricData;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.kstream.TransformerSupplier;

/**
 * an instance of {@link TransformerSupplier} that generates a {@link MetricDataTransformer}
 */
@RequiredArgsConstructor
@Data
public class MetricDataTransformerSupplier implements TransformerSupplier<String, MetricData, KeyValue<String, MapperResult>> {

    @NonNull
    private DetectorMapper detectorMapper;

    @NonNull
    private final String stateStoreName;

    @Override
    public Transformer<String, MetricData, KeyValue<String, MapperResult>> get() {
        return new MetricDataTransformer(detectorMapper, stateStoreName);
    }
}
