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
import com.expedia.aquila.detect.AquilaAnomalyDetector;
import com.expedia.aquila.core.model.PredictionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Worker to build models.
 *
 * @author Willie Wheeler
 * @author Karan Shah
 */
public final class AquilaTrainer {
    private static final Logger log = LoggerFactory.getLogger(AquilaTrainer.class);
    
    private TrainerContext trainerContext;
    
    public AquilaTrainer(TrainerContext trainerContext) {
        notNull(trainerContext, "trainerContext can't be null");
        this.trainerContext = trainerContext;
    }
    
    public AquilaAnomalyDetector train(TrainingTask task, MetricFrame data) {
        notNull(task, "task can't be null");
        notNull(data, "data can't be null");
        
        log.trace("Training model");
        final PredictionModelTrainer predModelTrainer = new PredictionModelTrainer(task.getParams());
        final PredictionModel predModel = predModelTrainer.train(data);
        return new AquilaAnomalyDetector(predModel);
    }
}
