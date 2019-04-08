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
import com.expedia.adaptivealerting.core.anomaly.AnomalyThresholds;
import com.expedia.metrics.MetricData;
import lombok.Getter;
import lombok.val;

import java.util.UUID;

import static com.expedia.adaptivealerting.core.anomaly.AnomalyLevel.MODEL_WARMUP;
import static com.expedia.adaptivealerting.core.anomaly.AnomalyLevel.NORMAL;
import static com.expedia.adaptivealerting.core.anomaly.AnomalyLevel.STRONG;
import static com.expedia.adaptivealerting.core.anomaly.AnomalyLevel.WEAK;
import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

/**
 * Anomaly detector implementation of
 * <a href="https://en.wikipedia.org/wiki/Shewhart_individuals_control_chart">Shewhart individuals control chart </a>
 * It uses the moving range (R) and the individual samples (X) to observe long-term and short-term variation in input
 * stream of data.
 *
 * @see <a href="https://www.spcforexcel.com/knowledge/variable-control-charts/individuals-control-charts">https://www.spcforexcel.com/knowledge/variable-control-charts/individuals-control-charts</a>
 */
public final class IndividualsControlChartDetector implements Detector {
    private static final double R_CONTROL_CHART_CONSTANT_D4 = 3.267;
    private static final double R_CONTROL_CHART_CONSTANT_D2 = 1.128;

    /**
     * Number of points after which limits will be recomputed
     */
    private static final int RECOMPUTE_LIMITS_PERIOD = 100;

    @Getter
    private UUID uuid;

    @Getter
    private IndividualsControlChartParams params;

    /**
     * Aggregate Moving range. Used to calculate avg. moving range.
     */
    private double movingRangeSum = 0.0;

    /**
     * Target predicted from average mean.
     */
    private double target;

    /**
     * Previous value.
     */
    private double prevValue;

    /**
     * Total number of received data points.
     */
    private int totalDataPoints = 1;

    /**
     * Upper limit for R chart
     */
    @Getter
    private double upperControlLimit_R;

    /**
     * Upper limit for X chart
     */
    @Getter
    private double upperControlLimit_X;

    /**
     * Lower limit for X chart
     */
    @Getter
    private double lowerControlLimit_X;

    /**
     * Variance estimate.
     */
    private double variance = 0.0;

    /**
     * Mean estimate.
     */
    private double mean;

    public IndividualsControlChartDetector(UUID uuid, IndividualsControlChartParams params) {
        notNull(uuid, "uuid can't be null");
        notNull(params, "params can't be null");
        this.uuid = uuid;
        this.params = params;
        this.prevValue = params.getInitValue();
        this.target = params.getInitValue();
        this.mean = params.getInitMeanEstimate();
    }

    @Override
    public AnomalyResult classify(MetricData metricData) {
        notNull(metricData, "metricData can't be null");

        val params = getParams();

        val observed = metricData.getValue();
        val stdDev = sqrt(this.variance);
//        val weakDelta = params.getWeakSigmas() * stdDev;
        val strongDelta = params.getStrongSigmas() * stdDev;

        val currentRange = Math.abs(prevValue - observed);

        // TODO Modify this to use AnomalyClassifier.classify() so we can get tail checks. [WLW]

        // Looks like currently this detector supports only a single anomaly level (strong).
        val thresholds = new AnomalyThresholds(
                this.mean + strongDelta,
                this.mean + strongDelta,
                this.mean - strongDelta,
                this.mean - strongDelta);

        AnomalyLevel level;

        if (totalDataPoints > params.getWarmUpPeriod()) {
            level = NORMAL;
            if (currentRange > upperControlLimit_R) {
                level = STRONG;
            } else {
                if (observed > upperControlLimit_X || observed < lowerControlLimit_X) {
                    level = WEAK;
                }
            }
        } else {
            level = MODEL_WARMUP;
        }

        if (level == NORMAL || level == MODEL_WARMUP) {
            this.movingRangeSum += abs(currentRange);
            this.mean = getRunningMean(observed);
            this.totalDataPoints++;
        }

        if (((totalDataPoints - params.getWarmUpPeriod()) % RECOMPUTE_LIMITS_PERIOD) == 0) {
            double averageMovingRange = getAverageMovingRange();
            double multiplier = params.getStrongSigmas() / R_CONTROL_CHART_CONSTANT_D2;
            this.target = this.mean;

            upperControlLimit_R = R_CONTROL_CHART_CONSTANT_D4 * averageMovingRange;
            upperControlLimit_X = this.target + multiplier * averageMovingRange;
            lowerControlLimit_X = this.target - multiplier * averageMovingRange;
        }
        this.prevValue = observed;

        final AnomalyResult result = new AnomalyResult(level);
        result.setPredicted(this.mean);
        result.setThresholds(thresholds);
        return result;
    }

    private double getRunningMean(double observed) {
        return this.mean + ((observed - this.mean) / (this.totalDataPoints + 1));
    }

    private double getAverageMovingRange() {
        return movingRangeSum / Math.max(1, totalDataPoints - 1);
    }
}
