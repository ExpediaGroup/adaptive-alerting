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
package com.expedia.aquila.train;

import com.expedia.adaptivealerting.core.data.MetricFrame;
import com.expedia.adaptivealerting.core.util.DateUtil;
import com.expedia.aquila.core.model.DecompType;
import com.expedia.aquila.core.model.MidpointModel;
import com.expedia.aquila.core.util.MathUtil;
import com.github.servicenow.ds.stats.stl.SeasonalTrendLoess;

import java.time.Instant;
import java.util.Arrays;

import static com.expedia.adaptivealerting.core.util.AssertUtil.isTrue;
import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Trains the midpoint model using the STL (Seasonal/Trend Decomposition Using Loess) algorithm. Supports both additive
 * and multiplicative decomposition.
 *
 * @author Willie Wheeler
 * @author Karan Shah
 */
public final class MidpointModelTrainer {
    private TrainingParams params;
    
    public MidpointModelTrainer(TrainingParams params) {
        notNull(params, "params can't be null");
        this.params = params;
    }
    
    public MidpointModel train(MetricFrame metricFrame) {
        notNull(metricFrame, "metricFrame can't be null");
        isTrue(metricFrame.getNumRows() > 0, "Required: metricFrame.numRows > 0");
        
        final Instant instant0 = Instant.ofEpochSecond(metricFrame.getMetricPoint(0).getEpochTimeInSeconds());
        final int tickOffset = DateUtil.tickOffsetFromWeekStart(instant0, params.tickSize());
        isTrue(tickOffset == 0, "Required: metricFrame must start from Sunday at midnight, UTC time");
        
        final double[] data = metricFrame.toDoubleValues();
        switch (params.decompType()) {
            case ADDITIVE:
                return trainAdditiveMidpointModel(data);
            case MULTIPLICATIVE:
                return trainMultiplicativeModel(data);
            default:
                throw new IllegalStateException("Illegal type: " + params.decompType());
        }
    }
    
    private MidpointModel trainAdditiveMidpointModel(double[] data) {
        final SeasonalTrendLoess.Decomposition stl = decompose(data);
        final double[] seasonal = wma(stl.getSeasonal());
        final double[] seasonal1w = Arrays.copyOfRange(seasonal, 0, params.periodSize());
        final double[] trend = wma(stl.getTrend());
        return new MidpointModel(DecompType.ADDITIVE, seasonal1w, trend[trend.length - 1]);
    }
    
    private MidpointModel trainMultiplicativeModel(double[] data) {
        final double[] logValues = MathUtil.incrAndLog(data);
        final SeasonalTrendLoess.Decomposition logStl = decompose(logValues);
        final double[] seasonal = wma(MathUtil.exp(logStl.getSeasonal()));
        final double[] seasonal1w = Arrays.copyOfRange(seasonal, 0, params.periodSize());
        final double[] trend = wma(MathUtil.expAndDecr(logStl.getTrend()));
        return new MidpointModel(DecompType.MULTIPLICATIVE, seasonal1w, trend[trend.length - 1]);
    }
    
    private SeasonalTrendLoess.Decomposition decompose(double[] data) {
        return new SeasonalTrendLoess.Builder()
                .setPeriodLength(params.periodSize())
                .setPeriodic()
                .setRobust()
                .buildSmoother(data)
                .decompose();
    }
    
    private double[] wma(double[] data) {
        return MathUtil.weightedMovingAverage(data, params.wmaWindowSize());
    }
}
