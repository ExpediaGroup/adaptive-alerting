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
package com.expedia.adaptivealerting.anomdetect.individuals;

import com.expedia.adaptivealerting.anomdetect.AnomalyDetector;
import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.metrics.MetricData;
import lombok.Data;
import lombok.NonNull;

import java.util.UUID;

import static com.expedia.adaptivealerting.core.anomaly.AnomalyLevel.*;
import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;
import static java.lang.Math.abs;

/**
 * Anomaly detector implementation of
 * <a href="https://en.wikipedia.org/wiki/Shewhart_individuals_control_chart">Shewhart individuals control chart </a>
 * It uses the moving range (R) and the individual samples (X) to observe long-term and short-term variation in input
 * stream of data.
 *
 * @author shsethi
 * @see <a href="https://www.spcforexcel.com/knowledge/variable-control-charts/individuals-control-charts">https://www.spcforexcel.com/knowledge/variable-control-charts/individuals-control-charts</a>
 */
@Data
public final class IndividualsControlChartDetector implements AnomalyDetector {
    private static final double R_CONTROL_CHART_CONSTANT_D4 = 3.267;
    private static final double R_CONTROL_CHART_CONSTANT_D2 = 1.128;


    /**
     * Number of points after which limits will be recomputed
     */
    private static final int RECOMPUTE_LIMITS_PERIOD = 100;

    @NonNull
    private UUID uuid;

    @NonNull
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
    private double upperControlLimit_R;

    /**
     * Upper limit for X chart
     */
    private double upperControlLimit_X;

    /**
     * Lower limit for X chart
     */
    private double lowerControlLimit_X;

    /**
     * Mean estimate.
     */
    private double mean;

    public IndividualsControlChartDetector() {
        this(UUID.randomUUID(), new IndividualsControlChartParams());
    }

    /**
     * Creates a new detector. Initial target is given by params.initValue and initial variance is 0.
     *
     * @param uuid   Detector UUID.
     * @param params Model params.
     */
    public IndividualsControlChartDetector(UUID uuid, IndividualsControlChartParams params) {
        notNull(uuid, "uuid can't be null");
        notNull(params, "params can't be null");

        this.uuid = uuid;
        this.params = params;
        this.prevValue = params.getInitValue();
        this.target = params.getInitValue();
    }

    @Override
    public AnomalyResult classify(MetricData metricData) {
        notNull(metricData, "metricData can't be null");

        final double observed = metricData.getValue();
        double currentRange = Math.abs(prevValue - observed);

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

        return new AnomalyResult(uuid, metricData, level);
    }

    private double getRunningMean(double observed) {
        return this.mean + ((observed - this.mean) / (this.totalDataPoints + 1));
    }

    private double getAverageMovingRange() {
        return movingRangeSum / Math.max(1, totalDataPoints - 1);
    }
}
