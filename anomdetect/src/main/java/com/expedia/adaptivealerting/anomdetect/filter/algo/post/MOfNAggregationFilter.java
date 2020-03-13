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
package com.expedia.adaptivealerting.anomdetect.filter.algo.post;

import com.expedia.adaptivealerting.anomdetect.detect.AnomalyLevel;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorResult;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.OutlierDetectorResult;
import com.expedia.adaptivealerting.anomdetect.filter.PostDetectionFilter;
import com.expedia.adaptivealerting.anomdetect.filter.chain.PostDetectionFilterChain;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Data
@AllArgsConstructor
@Setter(AccessLevel.NONE)
public class MOfNAggregationFilter implements PostDetectionFilter {
    private int m;
    private int n;
    private final AnomalyLevel[] buffer;
    private int bufferIndex = 0;

    @JsonCreator
    public MOfNAggregationFilter(@JsonProperty("m") int m, @JsonProperty("n") int n) {
        isTrue(m > 0, "Required: m > 0");
        isTrue(n >= m, "Required: n > m");

        this.m = m;
        this.n = n;
        this.buffer = new AnomalyLevel[getN()];
    }

    @Override
    public DetectorResult doFilter(DetectorResult result, PostDetectionFilterChain chain) {
        notNull(result, "result can't be null");

        if (result instanceof OutlierDetectorResult) {
            buffer[bufferIndex++] = result.getAnomalyLevel();
            if (bufferIndex >= getN()) {
                bufferIndex = 0;
            }
            val outlierDetectorResult = (OutlierDetectorResult) result;
            val aggregatedResult = new OutlierDetectorResult()
                    .setAnomalyLevel(result.getAnomalyLevel())
                    .setPredicted(outlierDetectorResult.getPredicted())
                    .setThresholds(outlierDetectorResult.getThresholds())
                    .setTrusted(result.isTrusted());

            if (numAnomalies() >= getM()) {
                aggregatedResult.setAnomalyLevel(AnomalyLevel.STRONG);
            }
            return chain.doFilter(aggregatedResult);
        } else {
            log.warn(String.format("%s can only be used with %s types of DetectorResults. Skipping aggregation.",
                    this.getClass().getSimpleName(),
                    OutlierDetectorResult.class.getSimpleName()));
            return chain.doFilter(result);
        }
    }

    private int numAnomalies() {
        int numAnomalies = 0;
        for (AnomalyLevel anomalyLevel : buffer) {
            if (anomalyLevel == AnomalyLevel.WEAK || anomalyLevel == AnomalyLevel.STRONG) {
                numAnomalies++;
            }
        }
        return numAnomalies;
    }
}
