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
import com.expedia.adaptivealerting.dataconnect.DataConnector;
import com.expedia.aquila.core.model.AquilaModel;
import com.expedia.aquila.core.model.AquilaModelMetadata;
import com.expedia.aquila.core.model.Classifier;
import com.expedia.aquila.core.model.PredictionModel;
import com.expedia.aquila.core.repo.AquilaModelRepo;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.TagCollection;
import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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

    private DataConnector dataConnector;
    private PredictionModelTrainer predictionModelTrainer;
    private AquilaModelRepo modelRepo;
    private RestTemplate restTemplate;
    private String fetchMetricUrl;

    @Autowired
    public AquilaTrainerService(
        DataConnector dataConnector,
        PredictionModelTrainer predictionModelTrainer,
        AquilaModelRepo modelRepo,
        RestTemplate restTemplate,
        Config connectorsConfig
    ) {
        this.dataConnector = dataConnector;
        this.predictionModelTrainer = predictionModelTrainer;
        this.modelRepo = modelRepo;
        this.restTemplate = restTemplate;
        this.fetchMetricUrl = connectorsConfig
                .getConfig("modelservice")
                .getString("fetchMetricUrl");
    }
    
    public AquilaModel train(TrainingRequest request) {
        notNull(request, "request can't be null");
        
        log.info("Training model: request={}", request);
        final MetricFrame data = loadTrainingData(request);
        final PredictionModel predictionModel = predictionModelTrainer.train(request.getParams(), data);
        final Classifier classifier = new Classifier();
        final AquilaModel aquilaModel = new AquilaModel(UUID.randomUUID(), predictionModel, classifier);
        
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
        MetricDefinition metricDefinition = request.getMetricDefinition();
        if (metricDefinition == null) {
            notNull(request.getMetricId(), "metricId can't be null if metricDefinition is null");
            metricDefinition = fetchMetricDefinition(request.getMetricId());
            notNull(metricDefinition, "metricId could not be fetched");
        }
        return dataConnector.load(metricDefinition, request.getStartDate(), request.getEndDate());
    }

    private MetricDefinition fetchMetricDefinition(String metricId) {
        log.info("Invoking model service to fetch MetricDefinition for id {}", metricId);
        try {
            Resource<Metric> metricResource = restTemplate.exchange(
                    fetchMetricUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Resource<Metric>>() { },
                    ImmutableMap.of("hash", metricId)
            ).getBody();

            Metric metricRs = metricResource == null ? null : metricResource.getContent();
            if (metricRs != null) {
                log.info("Received key {} and tags {} for id {}",
                        metricRs.getKey(), metricRs.getTags(), metricId);
                return new MetricDefinition(
                        metricRs.getKey(), new TagCollection(metricRs.getTags()), TagCollection.EMPTY);
            }
        } catch (RestClientException rce) {
            log.error("Tag fetch fail fo id=" + metricId, rce);
        }
        return null;
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
