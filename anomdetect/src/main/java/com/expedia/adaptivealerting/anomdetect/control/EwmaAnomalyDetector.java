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
package com.expedia.adaptivealerting.anomdetect.control;

import com.expedia.adaptivealerting.anomdetect.AnomalyDetector;
import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.anomaly.AnomalyThresholds;
import com.expedia.metrics.MetricData;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.isTrue;
import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;
import static java.lang.Math.sqrt;

/**
 * <p>
 * Anomaly detector based on the exponential weighted moving average (EWMA) chart, a type of control chart used in
 * statistical quality control. This is an online algorithm, meaning that it updates the thresholds incrementally as new
 * data comes in.
 * </p>
 * <p>
 * It takes a little while before the internal mean and variance estimates converge to something that makes sense. As a
 * rule of thumb, feed the detector 10 data points or so before using it for actual anomaly detection.
 * </p>
 *
 * @author Willie Wheeler
 * @see <a href="https://en.wikipedia.org/wiki/EWMA_chart">EWMA chart</a>
 * @see <a href="https://en.wikipedia.org/wiki/Moving_average#Exponentially_weighted_moving_variance_and_standard_deviation">Exponentially weighted moving average and standard deviation</a>
 * @see <a href="https://www.itl.nist.gov/div898/handbook/pmc/section3/pmc324.htm">EWMA Control Charts</a>
 */
@Data
@ToString
public final class EwmaAnomalyDetector implements AnomalyDetector {
    
    @Data
    @Accessors(chain = true)
    public static final class Params {
        
        /**
         * Smoothing param. Somewhat misnamed because higher values lead to less smoothing, but it's called the
         * smoothing parameter in the literature.
         */
        private double alpha = 0.15;
        
        /**
         * Weak threshold sigmas.
         */
        private double weakSigmas = 3.0;
        
        /**
         * Strong threshold sigmas.
         */
        private double strongSigmas = 4.0;
    
        /**
         * Initial mean estimate.
         */
        private double initMeanEstimate = 0.0;
        
        public void validate() {
            isTrue(0.0 <= alpha && alpha <= 1.0, "Required: alpha in the range [0, 1]");
            isTrue(weakSigmas > 0.0, "Required: weakSigmas > 0.0");
            isTrue(strongSigmas > weakSigmas, "Required: strongSigmas > weakSigmas");
        }
    }
    
    @NonNull
    private UUID uuid;
    
    @NonNull
    private Params params;

    /**
     * Mean estimate.
     */
    private double mean = 0.0;

    /**
     * Variance estimate.
     */
    private double variance = 0.0;
    
    public EwmaAnomalyDetector() {
        this(UUID.randomUUID(), new Params());
    }
    
    public EwmaAnomalyDetector(Params params) {
        this(UUID.randomUUID(), params);
    }
    
    public EwmaAnomalyDetector(UUID uuid, Params params) {
        notNull(uuid, "uuid can't be null");
        notNull(params, "params can't be null");
        
        params.validate();
        
        this.uuid = uuid;
        this.params = params;
        this.mean = params.initMeanEstimate;
    }
    
    @Override
    public AnomalyResult classify(MetricData metricData) {
        notNull(metricData, "metricData can't be null");
        
        final double observed = metricData.getValue();
        final double stdDev = sqrt(this.variance);
        final double weakDelta = params.weakSigmas * stdDev;
        final double strongDelta = params.strongSigmas * stdDev;
        
        final AnomalyThresholds thresholds = new AnomalyThresholds(
                this.mean + strongDelta,
                this.mean + weakDelta,
                this.mean - strongDelta,
                this.mean - weakDelta);
        
        updateEstimates(observed);
        
        final AnomalyLevel level = thresholds.classify(observed);
        
        final AnomalyResult result = new AnomalyResult(uuid, metricData, level);
        result.setPredicted(this.mean);
        result.setThresholds(thresholds);
        return result;
    }
    
    private void updateEstimates(double value) {
        
        // https://en.wikipedia.org/wiki/Moving_average#Exponentially_weighted_moving_variance_and_standard_deviation
        // http://people.ds.cam.ac.uk/fanf2/hermes/doc/antiforgery/stats.pdf
        final double diff = value - this.mean;
        final double incr = params.alpha * diff;
        this.mean += incr;
        
        // Welford's algorithm for computing the variance online
        // https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Online_algorithm
        // https://www.johndcook.com/blog/2008/09/26/comparing-three-methods-of-computing-standard-deviation/
        this.variance = (1.0 - params.alpha) * (this.variance + diff * incr);
    }
}
