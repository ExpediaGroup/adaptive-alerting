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
import com.expedia.aquila.core.model.DispersionModel;
import com.expedia.aquila.core.model.MidpointModel;
import com.expedia.aquila.core.model.PredictionModel;
import com.expedia.aquila.core.model.TrainingParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Algorithm for training Aquila prediction models.
 *
 * @author Willie Wheeler
 * @author Karan Shah
 */
@Component
public final class PredictionModelTrainer {
    
    @Autowired
    private MidpointModelTrainer midpointModelTrainer;
    
    @Autowired
    private DispersionModelTrainer dispModelTrainer;
    
    public PredictionModel train(TrainingParams params, MetricFrame metricFrame) {
        notNull(metricFrame, "metricFrame can't be null");
        final MidpointModel midpointModel = midpointModelTrainer.train(params, metricFrame);
        final DispersionModel dispModel = dispModelTrainer.train(params, midpointModel, metricFrame);
        return new PredictionModel(midpointModel, dispModel);
    }
}
