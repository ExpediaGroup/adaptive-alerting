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
package com.expedia.adaptivealerting.anomdetect.perf;

import com.expedia.adaptivealerting.anomdetect.AbstractAnomalyDetector;
import com.expedia.adaptivealerting.anomdetect.AnomalyDetector;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.metrics.MetricData;
import lombok.extern.slf4j.Slf4j;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Wrapper around {@link AnomalyDetector}. It feeds the performance monitor with a listener and pushes the
 * classification outputs to the perfmon every time a new {@link MetricData} comes in.
 *
 * @author kashah
 */
@Slf4j
public class MonitoredDetector extends AbstractAnomalyDetector {
    private AnomalyDetector detector;
    private PerformanceMonitor perfMonitor;
    
    public MonitoredDetector(AnomalyDetector detector, PerformanceMonitor perfMonitor) {
        
        // This wrapper becomes the model's detector.
        super(detector.getUuid());
        
        notNull(detector, "detector can't be null");
        notNull(perfMonitor, "perfMonitor can't be null");
        
        this.detector = detector;
        this.perfMonitor = perfMonitor;
    }
    
    @Override
    public AnomalyResult classify(MetricData metricData) {
        notNull(metricData, "metricData can't be null");
        final AnomalyResult result = detector.classify(metricData);
        perfMonitor.evaluatePerformance(result);
        return result;
    }
    
    public static class PerfMonHandler implements PerfMonListener {
        
        @Override
        public void processScore(double score) {
            log.info("Performance score: {}", score);
        }
    }
}
