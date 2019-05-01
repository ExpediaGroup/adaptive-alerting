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
package com.expedia.adaptivealerting.anomdetect.detector.aggregator.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.expedia.adaptivealerting.core.util.AssertUtil.isTrue;

@Data
@NoArgsConstructor
@Setter(AccessLevel.NONE)
public class MOfNAggregatorConfig implements AggregatorConfig {
    private int m;
    private int n;

    @JsonCreator
    public MOfNAggregatorConfig(
            @JsonProperty("m") int m,
            @JsonProperty("n") int n) {

        isTrue(m > 0, "Required: m > 0");
        isTrue(n >= m, "Required: n > m");

        this.m = m;
        this.n = n;
    }
}
