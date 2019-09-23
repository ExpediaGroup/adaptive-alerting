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
package com.expedia.adaptivealerting.anomdetect.forecast.point.algo;

import com.expedia.adaptivealerting.anomdetect.forecast.point.PointForecast;
import com.expedia.adaptivealerting.anomdetect.forecast.point.PointForecaster;
import com.expedia.metrics.MetricData;
import lombok.Generated;
import lombok.Getter;
import lombok.val;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

/**
 * Point forecaster based on the seasonal naive method described in
 * https://otexts.com/fpp2/simple-methods.html#simple-methods.
 */
public class SeasonalNaivePointForecaster implements PointForecaster {

    @Getter
    @Generated // https://reflectoring.io/100-percent-test-coverage/
    private SeasonalNaivePointForecasterParams params;

    // TODO Currently we use a very simple ring buffer, and ignore timestamps completely. So missing and duplicate
    //  values mess things up, though this gets flushed out over time as long as the issue isn't systematic. At any
    //  rate, we need to come up with a more robust approach that accounts for timestamps.
    private Double[] buffer;
    private int currIndex;

    /**
     * Creates a new forecaster from the given configuration parameters.
     *
     * @param params Configuration parameters.
     */
    public SeasonalNaivePointForecaster(SeasonalNaivePointForecasterParams params) {
        notNull(params, "params can't be null");
        params.validate();
        this.params = params;
        initState();
    }

    @Override
    public PointForecast forecast(MetricData metricData) {
        notNull(metricData, "metricData can't be null");
        val currForecastValue = buffer[currIndex];
        buffer[currIndex] = metricData.getValue();
        this.currIndex = (currIndex + 1) % buffer.length;
        return currForecastValue == null ? null : new PointForecast(currForecastValue, false);
    }

    private void initState() {
        val n = this.params.getCycleLength();
        this.buffer = new Double[n];
        this.currIndex = 0;
    }
}
