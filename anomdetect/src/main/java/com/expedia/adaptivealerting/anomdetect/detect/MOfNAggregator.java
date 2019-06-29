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
package com.expedia.adaptivealerting.anomdetect.detect;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.val;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isTrue;
import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

/**
 * m-of-n aggregator. The returned anomaly level is
 *
 * <ul>
 * <li>STRONG if at least m of the past n anomalies were either WEAK or STRONG;</li>
 * <li>otherwise, it's the anomaly level of the passed anomaly result.</li>
 * </ul>
 */
public class MOfNAggregator implements Aggregator {

    @Getter
    @Generated // https://reflectoring.io/100-percent-test-coverage/
    private Config config;

    private final AnomalyLevel[] buffer;
    private int bufferIndex = 0;

    /**
     * Creates a 3-of-5 aggregator.
     */
    public MOfNAggregator() {
        this(new Config(3, 5));
    }

    public MOfNAggregator(Config config) {
        notNull(config, "detector-docs can't be null");
        this.config = config;
        this.buffer = new AnomalyLevel[config.getN()];
    }

    @Override
    public AnomalyResult aggregate(AnomalyResult result) {
        notNull(result, "result can't be null");

        buffer[bufferIndex++] = result.getAnomalyLevel();
        if (bufferIndex >= config.getN()) {
            bufferIndex = 0;
        }

        val aggregatedResult = new AnomalyResult()
                .setAnomalyLevel(result.getAnomalyLevel())
                .setPredicted(result.getPredicted())
                .setThresholds(result.getThresholds());

        if (numAnomalies() >= config.getM()) {
            aggregatedResult.setAnomalyLevel(AnomalyLevel.STRONG);
        }

        return aggregatedResult;
    }

    private int numAnomalies() {
        int numAnomalies = 0;
        for (int i = 0; i < buffer.length; i++) {
            if (buffer[i] == AnomalyLevel.WEAK || buffer[i] == AnomalyLevel.STRONG) {
                numAnomalies++;
            }
        }
        return numAnomalies;
    }

    @Data
    @NoArgsConstructor
    @Setter(AccessLevel.NONE)
    public static class Config implements AggregatorConfig {
        private int m;
        private int n;

        @JsonCreator
        public Config(@JsonProperty("m") int m, @JsonProperty("n") int n) {
            isTrue(m > 0, "Required: m > 0");
            isTrue(n >= m, "Required: n > m");

            this.m = m;
            this.n = n;
        }
    }
}
