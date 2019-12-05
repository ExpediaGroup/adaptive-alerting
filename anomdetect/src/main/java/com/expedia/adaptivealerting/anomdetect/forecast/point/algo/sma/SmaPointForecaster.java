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
package com.expedia.adaptivealerting.anomdetect.forecast.point.algo.sma;

import com.expedia.adaptivealerting.anomdetect.forecast.point.PointForecast;
import com.expedia.adaptivealerting.anomdetect.forecast.point.PointForecaster;
import com.expedia.metrics.MetricData;
import com.google.common.collect.EvictingQueue;

import lombok.Generated;
import lombok.Getter;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

/**
 * Point forecaster based on the Simple Moving Average (SMA) method
 */
public class SmaPointForecaster implements PointForecaster {

    @Getter
    @Generated // https://reflectoring.io/100-percent-test-coverage/
    private SmaPointForecasterParams params;

    @Getter
    @Generated // https://reflectoring.io/100-percent-test-coverage/
    private double mean;

    @Getter
    @Generated // https://reflectoring.io/100-percent-test-coverage/
    private EvictingQueue<Double> periodOfValues;

    public SmaPointForecaster() {
        this(new SmaPointForecasterParams());
    }

    public SmaPointForecaster(SmaPointForecasterParams params) {
        notNull(params, "params can't be null");
        params.validate();
        this.params = params;
        this.periodOfValues = EvictingQueue.create(params.getLookBackPeriod());

        if (params.getInitialPeriodOfValues() != null) {
            params.getInitialPeriodOfValues().forEach(this::updateMeanEstimate);
        }
    }

    @Override
    public PointForecast forecast(MetricData metricData) {
        notNull(metricData, "metricData can't be null");
        updateMeanEstimate(metricData.getValue());

        return new PointForecast(mean, false);
    }

    private void updateMeanEstimate(double observed) {
        double meanSum = mean * periodOfValues.size();

        // remove the head's contribution to the mean's sum only if present and we have a full period of data
        Double head = periodOfValues.peek();
        if (head != null && periodOfValues.size() == params.getLookBackPeriod()) {
            meanSum -= head;
        }

        periodOfValues.add(observed);

        // add in the observed value's contribution to the mean's sum & recalculate the mean
        meanSum += observed;
        mean = meanSum / periodOfValues.size();
    }

}
