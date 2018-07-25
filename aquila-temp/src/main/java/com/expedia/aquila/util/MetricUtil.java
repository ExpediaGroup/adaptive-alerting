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
package com.expedia.aquila.util;

import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.data.Metric;
import com.expedia.adaptivealerting.core.data.MetricFrame;
import com.expedia.adaptivealerting.core.data.Mpoint;
import com.expedia.aquila.model.Classification;
import com.expedia.aquila.model.Prediction;
import com.expedia.www.haystack.commons.entities.MetricPoint;
import scala.collection.JavaConverters;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Metric-related utility functions (including metric points and anomaly results), isolated here to avoid cluttering up
 * the main code.
 *
 * @author Willie Wheeler
 * @author Karan Shah
 */
public final class MetricUtil {
    
    /**
     * Prevent instantiation.
     */
    private MetricUtil() {
    }
    
    public static Mpoint toMpoint(MetricPoint metricPoint) {
        final Metric metric = new Metric();
        metric.addTags(JavaConverters.mapAsJavaMap(metricPoint.tags()));
        
        final Mpoint mpoint = new Mpoint();
        mpoint.setMetric(metric);
        mpoint.setEpochTimeInSeconds(metricPoint.epochTimeInSeconds());
        mpoint.setValue(metricPoint.value());
        return mpoint;
    }
    
    public static AnomalyResult toAnomalyResult(Mpoint mpoint, Prediction prediction, Classification classification) {
        final double mean = prediction.getMean();
        final double stdev = prediction.getStdev();
        final double weakThreshold = classification.getWeakThresholdSigmas() * stdev;
        final double strongThreshold = classification.getStrongThresholdSigmas() * stdev;
        
        final AnomalyResult result = new AnomalyResult();
        
        result.setEpochSecond(mpoint.getEpochTimeInSeconds());
        result.setObserved(mpoint.getValue().doubleValue());
        
        result.setPredicted(mean);
        result.setWeakThresholdUpper(mean + weakThreshold);
        result.setWeakThresholdLower(mean - weakThreshold);
        result.setStrongThresholdUpper(mean + strongThreshold);
        result.setStrongThresholdLower(mean - strongThreshold);
        
        result.setAnomalyScore(classification.getAnomalyScore());
        result.setAnomalyLevel(classification.getAnomalyLevel());
        
        return result;
    }
    
    // TODO Move this to MetricFrame
    public static double[] toValues(MetricFrame metricFrame) {
        notNull(metricFrame, "metricFrame can't be null");
        final int n = metricFrame.getNumRows();
        final double[] values = new double[n];
        for (int i = 0; i < n; i++) {
            values[i] = metricFrame.getMetricPoint(i).getValue();
        }
        return values;
    }
}
