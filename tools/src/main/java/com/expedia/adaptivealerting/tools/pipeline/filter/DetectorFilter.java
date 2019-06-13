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
package com.expedia.adaptivealerting.tools.pipeline.filter;

import com.expedia.adaptivealerting.anomdetect.Detector;
import com.expedia.adaptivealerting.anomdetect.MappedMetricData;
import com.expedia.adaptivealerting.tools.pipeline.util.AnomalyResultSubscriber;
import com.expedia.adaptivealerting.tools.pipeline.util.MetricDataSubscriber;
import com.expedia.metrics.MetricData;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.LinkedList;
import java.util.List;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

/**
 * Stream filter that applies an outlier detector to metrics and generates outlier detector results.
 */
@Slf4j
public final class DetectorFilter implements MetricDataSubscriber {
    private final Detector detector;
    private final List<AnomalyResultSubscriber> subscribers = new LinkedList<>();

    public DetectorFilter(Detector detector) {
        notNull(detector, "detector can't be null");
        this.detector = detector;
    }

    @Override
    public void next(MetricData metricData) {
        notNull(metricData, "metricData can't be null");
        val anomaly = new MappedMetricData(metricData, detector.getUuid());
        val anomalyResult = detector.detect(metricData);
        anomaly.setAnomalyResult(anomalyResult);
        publish(anomaly);
    }

    public void addSubscriber(AnomalyResultSubscriber subscriber) {
        notNull(subscriber, "subscriber can't be null");
        subscribers.add(subscriber);
    }

    public void removeSubscriber(AnomalyResultSubscriber subscriber) {
        notNull(subscriber, "subscriber can't be null");
        subscribers.remove(subscriber);
    }

    private void publish(MappedMetricData anomaly) {
        subscribers.stream().forEach(subscriber -> subscriber.next(anomaly));
    }
}
