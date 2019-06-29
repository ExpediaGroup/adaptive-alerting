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
package com.expedia.adaptivealerting.anomdetect.forecast.algo;

import com.expedia.adaptivealerting.anomdetect.forecast.PointForecast;
import com.expedia.adaptivealerting.anomdetect.forecast.PointForecaster;
import com.expedia.metrics.MetricData;
import lombok.Generated;
import lombok.Getter;
import lombok.val;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

public class EwmaPointForecaster implements PointForecaster {

    @Getter
    @Generated // https://reflectoring.io/100-percent-test-coverage/
    private EwmaPointForecasterParams params;

    @Getter
    @Generated // https://reflectoring.io/100-percent-test-coverage/
    private double mean;

    public EwmaPointForecaster() {
        this(new EwmaPointForecasterParams());
    }

    public EwmaPointForecaster(EwmaPointForecasterParams params) {
        notNull(params, "params can't be null");
        params.validate();
        this.params = params;
        this.mean = params.getInitMeanEstimate();
    }

    @Override
    public PointForecast forecast(MetricData metricData) {
        notNull(metricData, "metricData can't be null");
        val observed = metricData.getValue();
        updateMeanEstimate(observed);

        // TODO Handle warmup
        return new PointForecast(mean, false);
    }

    private void updateMeanEstimate(double observed) {
        // https://en.wikipedia.org/wiki/Moving_average#Exponentially_weighted_moving_variance_and_standard_deviation
        // http://people.ds.cam.ac.uk/fanf2/hermes/doc/antiforgery/stats.pdf
        val residual = observed - this.mean;
        val incr = params.getAlpha() * residual;
        this.mean += incr;
    }

}
