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
package com.expedia.adaptivealerting.anomdetect.breakout;

import com.expedia.adaptivealerting.anomdetect.Detector;
import com.expedia.adaptivealerting.anomdetect.DetectorResult;
import com.expedia.metrics.MetricData;
import com.google.common.collect.EvictingQueue;
import lombok.Getter;
import lombok.val;

import java.util.UUID;
import java.util.stream.Collectors;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

public final class EdmxBreakoutDetector implements Detector {

    @Getter
    private UUID uuid;

    private final EvictingQueue<Double> buffer;
    private int delta;
    private int numPerms;

    public EdmxBreakoutDetector(UUID uuid, int bufferSize, int delta, int numPerms) {
        notNull(uuid, "uuid can't be null");
        this.uuid = uuid;
        this.buffer = EvictingQueue.create(bufferSize);
        this.delta = delta;
        this.numPerms = numPerms;
    }

    @Override
    public DetectorResult detect(MetricData metricData) {
        notNull(metricData, "metricData can't be null");
        buffer.add(metricData.getValue());

        if (buffer.size() < 2 * delta) {
            return new BreakoutDetectorResult(BreakoutDetectorResult.Type.WARMUP);
        }

        val list = buffer.stream().collect(Collectors.toList());
        val breakoutResult = Edmx.edmx(list, delta, numPerms);

        // TODO Calculate type and timestamp
        return new BreakoutDetectorResult(
                BreakoutDetectorResult.Type.BREAKOUT,
                breakoutResult.getLocation(),
                -1,
                breakoutResult.getStat());
    }
}
