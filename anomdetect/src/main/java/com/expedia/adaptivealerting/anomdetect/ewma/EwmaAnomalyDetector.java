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
package com.expedia.adaptivealerting.anomdetect.ewma;

import com.expedia.adaptivealerting.anomdetect.AbstractAnomalyDetector;
import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.anomaly.AnomalyThresholds;
import com.expedia.metrics.MetricData;
import lombok.Data;
import lombok.NonNull;

import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;
import static java.lang.Math.sqrt;

// TODO Add model warmup param and anomaly level. See e.g. CUSUM, Individuals, PEWMA. [WLW]

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
public final class EwmaAnomalyDetector extends AbstractAnomalyDetector<EwmaParams> {

    @NonNull
    private EwmaParams params;

    /**
     * Mean estimate.
     */
    private double mean = 0.0;

    /**
     * Variance estimate.
     */
    private double variance = 0.0;
    
    public EwmaAnomalyDetector() {
        this(UUID.randomUUID(), new EwmaParams());
    }
    
    public EwmaAnomalyDetector(UUID uuid) {
        this(uuid, new EwmaParams());
    }
    
    public EwmaAnomalyDetector(EwmaParams params) {
        this(UUID.randomUUID(), params);
    }
    
    public EwmaAnomalyDetector(UUID uuid, EwmaParams params) {
        notNull(uuid, "uuid can't be null");
        notNull(params, "params can't be null");
        
        params.validate();

        setUuid(uuid);
        loadParams(params);
    }

    @Override
    protected void loadParams(EwmaParams params) {
        this.params = params;
        this.mean = params.getInitMeanEstimate();
    }

    @Override
    protected Class<EwmaParams> getParamsClass() {
        return EwmaParams.class;
    }
    
    @Override
    public AnomalyResult classify(MetricData metricData) {
        notNull(metricData, "metricData can't be null");
        
        final double observed = metricData.getValue();
        final double stdDev = sqrt(this.variance);
        final double weakDelta = params.getWeakSigmas() * stdDev;
        final double strongDelta = params.getStrongSigmas() * stdDev;
        
        final AnomalyThresholds thresholds = new AnomalyThresholds(
                this.mean + strongDelta,
                this.mean + weakDelta,
                this.mean - strongDelta,
                this.mean - weakDelta);
        
        updateEstimates(observed);
        
        final AnomalyLevel level = thresholds.classify(observed);
        
        final AnomalyResult result = new AnomalyResult(getUuid(), metricData, level);
        result.setPredicted(this.mean);
        result.setThresholds(thresholds);
        return result;
    }
    
    private void updateEstimates(double value) {
        
        // https://en.wikipedia.org/wiki/Moving_average#Exponentially_weighted_moving_variance_and_standard_deviation
        // http://people.ds.cam.ac.uk/fanf2/hermes/doc/antiforgery/stats.pdf
        final double diff = value - this.mean;
        final double incr = params.getAlpha() * diff;
        this.mean += incr;
        
        // Welford's algorithm for computing the variance online
        // https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Online_algorithm
        // https://www.johndcook.com/blog/2008/09/26/comparing-three-methods-of-computing-standard-deviation/
        this.variance = (1.0 - params.getAlpha()) * (this.variance + diff * incr);
    }
}
