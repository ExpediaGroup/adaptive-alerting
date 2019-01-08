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
package com.expedia.aquila.train.service;

import com.expedia.adaptivealerting.core.data.MetricFrame;
import com.expedia.adaptivealerting.core.util.DateUtil;
import com.expedia.aquila.core.model.DecompType;
import com.expedia.aquila.core.model.MidpointModel;
import com.expedia.aquila.core.model.TrainingParams;
import com.expedia.aquila.core.util.MathUtil;
import com.github.servicenow.ds.stats.stl.SeasonalTrendLoess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
@Component
@Slf4j
public final class MidpointModelTrainer {
    
    public MidpointModel train(TrainingParams params, MetricFrame metricFrame) {
        notNull(metricFrame, "metricFrame can't be null");
        isTrue(metricFrame.getNumRows() > 0, "Required: metricFrame.numRows > 0");
        
        final Instant instant0 = Instant.ofEpochSecond(metricFrame.getMetricDataPoint(0).getTimestamp());
        log.info("Training midpoint model: params={}, startInstant={}", params, instant0);
        final int tickOffset = DateUtil.tickOffsetFromWeekStart(instant0, params.getIntervalInMinutes());
        isTrue(tickOffset == 0, "Required: metricFrame must start from Sunday at midnight, UTC time");
        
        final double[] data = metricFrame.toDoubleValues();
        switch (params.getDecompType()) {
            case ADDITIVE:
                return trainAdditiveMidpointModel(params, data);
            case MULTIPLICATIVE:
                return trainMultiplicativeModel(params, data);
            default:
                throw new IllegalStateException("Illegal type: " + params.getDecompType());
        }
    }
    
    private MidpointModel trainAdditiveMidpointModel(TrainingParams params, double[] data) {
        final SeasonalTrendLoess.Decomposition stl = decompose(params, data);
        final double[] seasonal = wma(params, stl.getSeasonal());
        final double[] seasonal1w = Arrays.copyOfRange(seasonal, 0, params.getPeriodSize());
        final double[] trend = wma(params, stl.getTrend());
        return new MidpointModel(DecompType.ADDITIVE, seasonal1w, trend[trend.length - 1]);
    }
    
    private MidpointModel trainMultiplicativeModel(TrainingParams params, double[] data) {
        final double[] logValues = MathUtil.incrAndLog(data);
        final SeasonalTrendLoess.Decomposition logStl = decompose(params, logValues);
        final double[] seasonal = wma(params, MathUtil.exp(logStl.getSeasonal()));
        final double[] seasonal1w = Arrays.copyOfRange(seasonal, 0, params.getPeriodSize());
        final double[] trend = wma(params, MathUtil.expAndDecr(logStl.getTrend()));
        return new MidpointModel(DecompType.MULTIPLICATIVE, seasonal1w, trend[trend.length - 1]);
    }
    
    private SeasonalTrendLoess.Decomposition decompose(TrainingParams params, double[] data) {
        return new SeasonalTrendLoess.Builder()
                .setPeriodLength(params.getPeriodSize())
                .setPeriodic()
                .setRobust()
                .buildSmoother(data)
                .decompose();
    }
    
    private double[] wma(TrainingParams params, double[] data) {
        return MathUtil.weightedMovingAverage(data, params.getWmaWindowSize());
    }
}
