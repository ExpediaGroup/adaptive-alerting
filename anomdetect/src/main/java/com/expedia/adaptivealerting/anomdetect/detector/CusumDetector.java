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
package com.expedia.adaptivealerting.anomdetect.detector;

import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.metrics.MetricData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;

import static com.expedia.adaptivealerting.core.anomaly.AnomalyLevel.*;
import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * <p>
 * Anomaly detector based on the cumulative sum. This is an online algorithm, meaning that it updates the intervalForecast
 * incrementally as new data comes in.
 * </p>
 * <p>
 * https://www.spcforexcel.com/knowledge/variable-control-charts/keeping-process-target-cusum-charts
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public final class CusumDetector extends AbstractDetector<CusumParams> {
    private static final double STD_DEV_DIVISOR = 1.128;

    /**
     * Total number of data points seen so far.
     */
    private int totalDataPoints = 1;

    /**
     * Cumulative sum on the high side. SH
     */
    private double sumHigh = 0.0;

    /**
     * Cumulative sum on the low side. SL
     */
    private double sumLow = 0.0;

    /**
     * Moving range. Used to estimate the standard deviation.
     */
    private double movingRange = 0.0;

    /**
     * Previous value.
     */
    private double prevValue = 0.0;

    public CusumDetector() {
        super(CusumParams.class);
    }

    @Override
    protected void initState(CusumParams params) {
        this.prevValue = params.getInitMeanEstimate();
    }

    @Override
    public AnomalyResult classify(MetricData metricData) {
        notNull(metricData, "metricData can't be null");

        val params = getParams();
        val observed = metricData.getValue();

        this.movingRange += Math.abs(this.prevValue - observed);

        val stdDev = avgMovingRange() / STD_DEV_DIVISOR;
        val slack = params.getSlackParam() * stdDev;
        val weakDelta = params.getWeakSigmas() * stdDev;
        val strongDelta = params.getStrongSigmas() * stdDev;

        this.sumHigh = Math.max(0, this.sumHigh + observed - (params.getTargetValue() + slack));
        this.sumLow = Math.min(0, this.sumLow + observed - (params.getTargetValue() - slack));

        this.prevValue = observed;

        // FIXME This eventually overflows. Realistically it won't happen, but would be nice to fix it anyway. [WLW]
        this.totalDataPoints++;

        Double upperStrong;
        Double upperWeak;
        Double lowerStrong;
        Double lowerWeak;
        AnomalyLevel level;

        if (totalDataPoints <= params.getWarmUpPeriod()) {
            level = MODEL_WARMUP;
        } else {
            level = NORMAL;

            upperWeak = weakDelta;
            upperStrong = strongDelta;
            lowerWeak = -weakDelta;
            lowerStrong = -strongDelta;

            // Below we use strict inequalities for anomaly checks instead of nonstrict.
            // The reason is that if a metric runs at a constant value, then the stdev is
            // 0, and another incoming metric data with the same constant value should
            // come out as NORMAL, not STRONG. [WLW]

            switch (params.getType()) {
                case LEFT_TAILED:
                    lowerWeak = -weakDelta;
                    lowerStrong = -strongDelta;
                    if (this.sumLow < lowerStrong) {
                        level = STRONG;
                        // TODO Check whether this is really what we are supposed to do here. [WLW]
                        resetSums();
                    } else if (this.sumLow < lowerWeak) {
                        level = WEAK;
                    }
                    break;
                case RIGHT_TAILED:
                    if (this.sumHigh > upperStrong) {
                        level = STRONG;
                        // TODO Check whether this is really what we are supposed to do here. [WLW]
                        resetSums();
                    } else if (this.sumHigh > upperWeak) {
                        level = WEAK;
                    }
                    break;
                case TWO_TAILED:
                    if (this.sumHigh > upperStrong || this.sumLow < lowerStrong) {
                        level = STRONG;
                        // TODO Check whether this is really what we are supposed to do here. [WLW]
                        resetSums();
                    } else if (this.sumHigh > upperWeak || this.sumLow < lowerWeak) {
                        level = WEAK;
                    }
                    break;
                default:
                    throw new IllegalStateException("Illegal type: " + params.getType());
            }
        }

        return new AnomalyResult(level);
    }

    private void resetSums() {
        this.sumHigh = 0.0;
        this.sumLow = 0.0;
    }

    private double avgMovingRange() {
        if (totalDataPoints > 1) {
            return movingRange / (totalDataPoints - 1);
        }
        return movingRange;
    }
}
