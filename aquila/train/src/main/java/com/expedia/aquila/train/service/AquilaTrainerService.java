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
package com.expedia.aquila.train.service;

import com.expedia.adaptivealerting.core.data.MetricFrame;
import com.expedia.adaptivealerting.dataconnect.DataConnector;
import com.expedia.aquila.core.model.AquilaModel;
import com.expedia.aquila.core.model.AquilaModelMetadata;
import com.expedia.aquila.core.model.PredictionModel;
import com.expedia.aquila.core.repo.AquilaModelRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Worker to build models.
 *
 * @author Willie Wheeler
 * @author Karan Shah
 */
@Service
@Slf4j
public final class AquilaTrainerService {
    
    @Autowired
    private DataConnector dataConnector;
    
    @Autowired
    private PredictionModelTrainer predictionModelTrainer;
    
    @Autowired
    private AquilaModelRepo modelRepo;
    
    public AquilaModel train(TrainingRequest request) {
        notNull(request, "request can't be null");
        
        log.info("Training model: request={}", request);
        final MetricFrame data = loadTrainingData(request);
        final PredictionModel predictionModel = predictionModelTrainer.train(request.getParams(), data);
        final AquilaModel aquilaModel = new AquilaModel(UUID.randomUUID(), predictionModel);
        
        final AquilaModelMetadata metadata = toMetadata(request);
        
        // TODO Not sure we want to do this, especially for the model search/HPO use case.
        // If we do save it, we need some way to flag whether it's a model under evaluation or a "good" model. [WLW]
        log.info("Saving model: detectorUuid={}, request={}", request);
        modelRepo.save(aquilaModel, metadata);
        log.info("Saved model: detectorUuid={}", aquilaModel.getDetectorUuid());
        
        return aquilaModel;
    }
    
    private MetricFrame loadTrainingData(TrainingRequest request) {
        log.trace("Loading training data: request={}", request);
        return dataConnector.load(request.getMetricDefinition(), request.getStartDate(), request.getEndDate());
    }
    
    private AquilaModelMetadata toMetadata(TrainingRequest request) {
        final Instant startDate = request.getStartDate();
        final Instant endDate = request.getEndDate();
        final Instant trainDate = Instant.now();
        
        final AquilaModelMetadata metadata = new AquilaModelMetadata();
        
        metadata.setMetricDefinition(request.getMetricDefinition());
        metadata.setTrainingParams(request.getParams());
    
        metadata.setStartDateUtc(startDate.toString());
        metadata.setEndDateUtc(endDate.toString());
        metadata.setTrainDateUtc(trainDate.toString());
        
        metadata.setStartEpochSecond(startDate.getEpochSecond());
        metadata.setEndEpochSecond(endDate.getEpochSecond());
        metadata.setTrainEpochSecond(trainDate.getEpochSecond());
        
        return metadata;
    }
}
