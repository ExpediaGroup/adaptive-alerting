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
package com.expedia.adaptivealerting.anomdetect.detect.algo;

import com.expedia.adaptivealerting.anomdetect.detect.Detector;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorResult;
import com.expedia.metrics.MetricData;
import com.google.common.collect.EvictingQueue;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

@ToString(callSuper = true)
@Slf4j
public final class EdmxBreakoutDetector implements Detector {

    @Getter
    private UUID uuid;

    @Getter
    private EdmxBreakoutDetectorHyperparams hyperparams;

    private final EvictingQueue<MetricData> buffer;

    public EdmxBreakoutDetector(UUID uuid, EdmxBreakoutDetectorHyperparams hyperparams) {
        notNull(uuid, "uuid can't be null");
        notNull(hyperparams, "hyperparams can't be null");
        hyperparams.validate();

        this.uuid = uuid;
        this.hyperparams = hyperparams;
        this.buffer = EvictingQueue.create(hyperparams.getBufferSize());
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
        val estimate = EdmxBreakoutEstimator.estimate(
                mdValues,
                hyperparams.getDelta(),
                hyperparams.getNumPerms(),
                hyperparams.getAlpha());

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
                .setPreBreakoutMedian(estimate.getPreBreakoutMedian())
                .setPostBreakoutMedian(estimate.getPostBreakoutMedian())
                .setPValue(estimate.getPValue())
                .setAlpha(estimate.getAlpha());
    }

}
