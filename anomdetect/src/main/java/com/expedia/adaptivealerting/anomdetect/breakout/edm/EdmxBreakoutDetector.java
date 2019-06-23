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
package com.expedia.adaptivealerting.anomdetect.breakout.edm;

import com.expedia.adaptivealerting.anomdetect.Detector;
import com.expedia.adaptivealerting.anomdetect.DetectorConfig;
import com.expedia.adaptivealerting.anomdetect.DetectorResult;
import com.expedia.metrics.MetricData;
import com.google.common.collect.EvictingQueue;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isBetween;
import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isTrue;
import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

@Slf4j
public final class EdmxBreakoutDetector implements Detector {

    @Getter
    private UUID uuid;

    @Getter
    private Params params;

    private final EvictingQueue<MetricData> buffer;

    public EdmxBreakoutDetector(UUID uuid, Params params) {
        notNull(uuid, "uuid can't be null");
        notNull(params, "params can't be null");

        this.uuid = uuid;
        this.params = params;
        this.buffer = EvictingQueue.create(params.bufferSize);
    }

    @Override
    public DetectorResult detect(MetricData metricData) {
        notNull(metricData, "metricData can't be null");
        buffer.add(metricData);

        val warmup = buffer.remainingCapacity() > 0;

        if (warmup) {
            return new EdmxBreakoutDetectorResult().setWarmup(true);
        }

        val mdValues = buffer.stream().mapToDouble(md -> md.getValue()).toArray();
        val estimate = EdmxBreakoutEstimator.estimate(mdValues, params.delta, params.numPerms, params.alpha);

        val mdList = buffer.stream().collect(Collectors.toList());
        val location = estimate.getLocation();

        if (location == -1) {
            return new EdmxBreakoutDetectorResult().setWarmup(false);
        }

        val epochSeconds = mdList.get(location).getTimestamp();
        val instant = Instant.ofEpochSecond(epochSeconds);

        return new EdmxBreakoutDetectorResult()
                .setWarmup(false)
                .setTimestamp(instant)
                .setSignificant(estimate.isSignificant())
                .setEnergyDistance(estimate.getEnergyDistance())
                .setPValue(estimate.getPValue())
                .setAlpha(estimate.getAlpha());
    }

    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = false)
    public static final class Params implements DetectorConfig {
        private int bufferSize;
        private int delta;
        private int numPerms;
        private double alpha;

        @Override
        public void validate() {
            isTrue(bufferSize >= 2 * delta, "Required: bufferSize >= 2 * delta");
            isTrue(delta > 0, "Required; delta > 0");
            isTrue(numPerms >= 0, "Required: numPerms >= 0");
            isBetween(alpha, 0.0, 1.0, "Required: 0.0 <= alpha <= 1.0");
        }
    }
}
