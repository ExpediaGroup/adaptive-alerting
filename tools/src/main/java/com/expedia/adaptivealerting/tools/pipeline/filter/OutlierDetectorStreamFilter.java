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

import com.expedia.adaptivealerting.core.detector.OutlierDetector;
import com.expedia.adaptivealerting.core.detector.OutlierDetectorResult;
import com.expedia.adaptivealerting.tools.pipeline.StreamPublisherSupport;
import com.expedia.adaptivealerting.tools.pipeline.StreamSubscriber;
import com.expedia.www.haystack.commons.entities.MetricPoint;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Stream filter that applies an outlier detector to metrics and generates outlier detector results.
 *
 * @author Willie Wheeler
 */
public final class OutlierDetectorStreamFilter implements StreamSubscriber<MetricPoint> {
    private final OutlierDetector outlierDetector;
    private final StreamPublisherSupport<OutlierDetectorResult> publisherSupport = new StreamPublisherSupport<>();
    
    public OutlierDetectorStreamFilter(OutlierDetector outlierDetector) {
        notNull(outlierDetector, "outlierDetector can't be null");
        this.outlierDetector = outlierDetector;
    }
    
    @Override
    public void next(MetricPoint metricPoint) {
        notNull(metricPoint, "metricPoint can't be null");
        publisherSupport.publish(outlierDetector.classify(metricPoint));
    }
    
    public void addSubscriber(StreamSubscriber<OutlierDetectorResult> subscriber) {
        notNull(subscriber, "subscriber can't be null");
        publisherSupport.addSubscriber(subscriber);
    }
    
    public void removeSubscriber(StreamSubscriber<OutlierDetectorResult> subscriber) {
        notNull(subscriber, "subscriber can't be null");
        publisherSupport.removeSubscriber(subscriber);
    }
}
