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

import com.expedia.adaptivealerting.anomdetect.detector.aggregator.config.MOfNAggregatorConfig;
import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import lombok.Generated;
import lombok.Getter;
import lombok.val;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

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
    private MOfNAggregatorConfig config;

    private final AnomalyLevel[] buffer;
    private int bufferIndex = 0;

    /**
     * Creates a 3-of-5 aggregator.
     */
    public MOfNAggregator() {
        this(new MOfNAggregatorConfig(3, 5));
    }

    public MOfNAggregator(MOfNAggregatorConfig config) {
        notNull(config, "config can't be null");
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
}
