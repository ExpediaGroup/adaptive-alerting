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

import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.data.MappedMpoint;
import com.expedia.adaptivealerting.core.data.Mpoint;
import com.expedia.adaptivealerting.core.util.AssertUtil;
import com.expedia.adaptivealerting.core.util.MetricUtil;
import com.expedia.www.haystack.commons.entities.MetricPoint;

import static com.expedia.adaptivealerting.core.anomaly.AnomalyLevel.*;
import static java.lang.Math.abs;

/**
 * <p>
 * </p>
 *
 * @author shsethi
 */
public class ShewhartIndividuals implements AnomalyDetector {

    private static final double R_CONTROL_CHART_CONSTANT_D4 = 3.267;
    private static final int RECOMPUTE_LIMITS_PERIOD = 25;

    /**
     * Local Aggregate Moving range. Used to calculate avg. moving range.
     */
    private double movingRangeSum;

    /**
     * Local target predicted from average mean.
     */
    private double target;

    /**
     * Local previous value.
     */
    private double prevValue;

    /**
     * Local total no of received data points.
     */
    private int totalDataPoints;

    /**
     * Local warm up period value. Minimum no of data points required before it can be used for actual anomaly
     * detection.
     */
    private int warmUpPeriod;

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
     * Local target
     */
    private double runningMean;

    /**
     * Creates a new MovingRangeChart detector initValue = 0.0, warmUpPeriod = 25
     */
    public ShewhartIndividuals() {
        this(0.0, 25);
    }

    /**
     * Creates a new MovingRangeChart detector. Initial target is given by initValue and initial variance is 0.
     *
     * @param initValue    Initial observation, used to set the first target estimate.
     * @param warmUpPeriod Warm up period value. Minimum no of data points required before it can be used for actual anomaly
     *                     detection.
     */
    public ShewhartIndividuals(
            double initValue,
            int warmUpPeriod
    ) {
        this.prevValue = initValue;
        this.warmUpPeriod = warmUpPeriod;
        this.movingRangeSum = 0.0;
        this.totalDataPoints = 1;
        this.target = initValue;
    }

    public int getWarmUpPeriod() {
        return warmUpPeriod;
    }


    @Override
    public AnomalyResult classify(MetricPoint metricPoint) {
        AssertUtil.notNull(metricPoint, "metricPoint can't be null");

        final double observed = metricPoint.value();
        double currentRange = abs(prevValue - observed);
        this.movingRangeSum += abs(currentRange);
        this.runningMean = getRunningMean(observed);

        final double dist = abs(observed - target);
        this.totalDataPoints++;

        AnomalyLevel anomalyLevel = UNKNOWN;
        if (totalDataPoints <= warmUpPeriod || ((totalDataPoints-warmUpPeriod) % RECOMPUTE_LIMITS_PERIOD )==0) {

            double averageMovingRange = getAverageMovingRange();
            this.target = this.runningMean;

            upperControlLimit_R = R_CONTROL_CHART_CONSTANT_D4 * averageMovingRange;
            upperControlLimit_X = this.target + 2.66 * averageMovingRange;
            lowerControlLimit_X = this.target - 2.66 * averageMovingRange;

        } else {
            anomalyLevel = NORMAL;

            if (currentRange > upperControlLimit_R) {
                anomalyLevel = STRONG;
            } else {
                if (observed > upperControlLimit_X || observed < lowerControlLimit_X) {
                    anomalyLevel = WEAK;
                }
            }
        }

        this.prevValue = observed;

        final Mpoint mpoint = MetricUtil.toMpoint(metricPoint);

        final AnomalyResult result = new AnomalyResult();
        result.setMetric(mpoint.getMetric());
        result.setDetectorId(this.getId());
        result.setEpochSecond(mpoint.getEpochTimeInSeconds());
        result.setObserved(observed);
        result.setPredicted(this.target);
        result.setWeakThresholdUpper(upperControlLimit_X);
        result.setStrongThresholdUpper(upperControlLimit_X);
        result.setWeakThresholdLower(lowerControlLimit_X);
        result.setStrongThresholdLower(lowerControlLimit_X);
        result.setAnomalyScore(dist);
        result.setAnomalyLevel(anomalyLevel);
        return result;
    }

    private double getRunningMean(double observed) {
        return ( (this.runningMean * this.totalDataPoints) + observed) / (this.totalDataPoints + 1);
    }

    @Override
    public MappedMpoint classify(MappedMpoint mappedMpoint) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private double getAverageMovingRange() {
        if (totalDataPoints > 1) {
            return movingRangeSum / (totalDataPoints - 1);
        }
        return movingRangeSum;
    }


}
