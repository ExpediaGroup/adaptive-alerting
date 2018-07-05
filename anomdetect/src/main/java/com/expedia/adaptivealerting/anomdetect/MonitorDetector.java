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
package com.expedia.adaptivealerting.anomdetect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.expedia.adaptivealerting.anomdetect.PerformanceMonitor.Performance;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.data.MappedMpoint;

/**
 * <p>
 * Monitor detector: Wrapper around Anomaly detector.
 * </p>
 *
 * @author kashah
 */
public class MonitorDetector {

    /**
     * Local Anomaly detector.
     */
    private AnomalyDetector detector;

    /**
     * Local Performance monitor.
     */
    private PerformanceMonitor perfMonitor;

    private Logger LOGGER = LoggerFactory.getLogger(MonitorDetector.class);

    public MonitorDetector(AnomalyDetector detector, PerformanceMonitor perfMonitor) {
        this.detector = detector;
        this.perfMonitor = perfMonitor;
    }

    // FIXME Do we need to return a mapped point here? [KS]
    public void classify(MappedMpoint mappedMpoint) {
        // FIXME Check if we need classify the mapped point again here. [KS]
        MappedMpoint mappedPoint = detector.classify(mappedMpoint);
        AnomalyResult result = mappedPoint.getAnomalyResult();
        Performance performance = perfMonitor.evaluatePerformance(result);
        if (performance != Performance.GOOD) {
            LOGGER.info("Rebuild model. Sending info to model-builder kafka topic:{}",
                    mappedPoint.getAnomalyResult().getMetric());
        }
    }

}
