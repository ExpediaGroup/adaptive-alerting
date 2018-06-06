/*
 * Copyright 2018 Expedia Group, Inc.
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

import com.expedia.adaptivealerting.anomdetect.AnomalyDetector;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.tools.pipeline.AnomalyResultSubscriber;
import com.expedia.adaptivealerting.tools.pipeline.MetricPointSubscriber;
import com.expedia.www.haystack.commons.entities.MetricPoint;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

import java.util.LinkedList;
import java.util.List;

/**
 * Stream filter that applies an outlier detector to metrics and generates outlier detector results.
 *
 * @author Willie Wheeler
 */
public final class AnomalyDetectorStreamFilter implements MetricPointSubscriber {
    private final AnomalyDetector anomalyDetector;
    private final List<AnomalyResultSubscriber> subscribers = new LinkedList<>();

    public AnomalyDetectorStreamFilter(AnomalyDetector anomalyDetector) {
        notNull(anomalyDetector, "anomalyDetector can't be null");
        this.anomalyDetector = anomalyDetector;
    }

    @Override
    public void next(MetricPoint metricPoint) {
        notNull(metricPoint, "metricPoint can't be null");
        publish(anomalyDetector.classify(metricPoint));
    }

    public void addSubscriber(AnomalyResultSubscriber subscriber) {
        notNull(subscriber, "subscriber can't be null");
        subscribers.add(subscriber);
    }

    public void removeSubscriber(AnomalyResultSubscriber subscriber) {
        notNull(subscriber, "subscriber can't be null");
        subscribers.remove(subscriber);
    }

    private void publish(AnomalyResult anomalyResult) {
        subscribers.stream().forEach(subscriber -> subscriber.next(anomalyResult));
    }
}
