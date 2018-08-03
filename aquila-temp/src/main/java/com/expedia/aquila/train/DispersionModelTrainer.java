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
import com.expedia.aquila.core.model.DispersionModel;
import com.expedia.aquila.core.model.MidpointModel;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * <p>
 * Trains the dispersion model.
 * </p>
 * <p>
 * For now "training" simply returns a hardcoded model. Eventually we'll fit a real model to the midpoint residuals.
 * </p>
 *
 * @author Willie Wheeler
 * @author Karan Shah
 */
public final class DispersionModelTrainer {
    
    // TODO Replace with an actual fit
    private static final double HARDCODED_ALPHA = 1.5;
    private static final double HARDCODED_BETA = 0.5;
    
    private TrainingParams params;
    
    public DispersionModelTrainer(TrainingParams params) {
        notNull(params, "params can't be null");
        this.params = params;
    }
    
    public DispersionModel train(MetricFrame metricFrame, MidpointModel midpointModel) {
        return new DispersionModel(HARDCODED_ALPHA, HARDCODED_BETA);
    }
}
