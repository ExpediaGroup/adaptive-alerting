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
package com.expedia.adaptivealerting.anomdetect.detector.aggregator;

import com.expedia.adaptivealerting.anomdetect.detector.AnomalyResult;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

/**
 * "Aggregator" that simply returns the passed {@link AnomalyResult}.
 */
public class PassThroughAggregator implements Aggregator {

    @Override
    public AnomalyResult aggregate(AnomalyResult result) {
        notNull(result, "result can't be null");
        return result;
    }

    /**
     * Dummy config for config deserialization. We don't actually use it for anything since it has no config params.
     */
    @Data
    @NoArgsConstructor
    @Setter(AccessLevel.NONE)
    public static class Config implements AggregatorConfig {
    }
}
