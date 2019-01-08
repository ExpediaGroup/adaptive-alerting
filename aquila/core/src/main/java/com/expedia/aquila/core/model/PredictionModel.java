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
package com.expedia.aquila.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Aquila prediction model. Contains an internal model of both a single weekly cycle and a trend. The trend is a single
 * value. Depending on your time series, you will want to retrain the model periodically to avoid the trend going stale.
 * (In principle the seasonal component can go stale too, though this would usually evolve over a longer timeframe.)
 *
 * @author Willie Wheeler
 * @author Karan Shah
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public final class PredictionModel {
    private MidpointModel midpointModel;
    private DispersionModel dispersionModel;
    
    public Prediction predict(Instant instant) {
        notNull(instant, "instant can't be null");
        final double mean = midpointModel.predict(instant);
        final double disp = dispersionModel.getDispersion(mean);
        return new Prediction(mean, disp);
    }
}
