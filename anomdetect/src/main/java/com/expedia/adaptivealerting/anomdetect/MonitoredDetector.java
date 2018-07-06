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
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.data.MappedMpoint;
import com.expedia.adaptivealerting.core.evaluator.RmseEvaluator;
import com.expedia.adaptivealerting.core.util.AssertUtil;
import com.expedia.www.haystack.commons.entities.MetricPoint;

/**
 * <p>
 * Wrapper around Anomaly detector. It feeds the performance monitor with a listener and pushes the classification
 * outputs to the perfmon every time a new mappedMpoint comes in.
 * </p>
 *
 * @author kashah
 */
public class MonitoredDetector implements AnomalyDetector {

    /**
     * Local Anomaly detector.
     */
    private AnomalyDetector detector;
    

    /**
     * Local Performance monitor.
     */
    private PerformanceMonitor perfMonitor;

    private static Logger LOGGER = LoggerFactory.getLogger(MonitoredDetector.class);

    public MonitoredDetector(AnomalyDetector detector, PerformanceMonitor perfMonitor) {
        AssertUtil.notNull(detector, "detector can't be null");
        AssertUtil.notNull(perfMonitor, "perfMonitor can't be null");
        this.detector = detector;
        this.perfMonitor = perfMonitor;
    }

    @Override
    public MappedMpoint classify(MappedMpoint mappedMpoint) {
        MappedMpoint mappedPoint = detector.classify(mappedMpoint);
        AnomalyResult result = mappedMpoint.getAnomalyResult();
        perfMonitor.evaluatePerformance(result);
        return mappedPoint;
    }

    @Override
    public AnomalyResult classify(MetricPoint metricPoint) {
        throw new UnsupportedOperationException("Not valid");
    }

    public static class PerfMonHandler implements PerfMonListener {
        @Override
        public void processScore(double score) {
            LOGGER.info("Performance score:{}", score);
        }
    }
}
