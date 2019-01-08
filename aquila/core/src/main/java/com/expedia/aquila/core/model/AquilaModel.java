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

import com.expedia.adaptivealerting.core.anomaly.AnomalyThresholds;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Aquila anomaly detector.
 *
 * @author Willie Wheeler
 * @author Karan Shah
 */
@Data
@Slf4j
public final class AquilaModel {
    private UUID detectorUuid;
    private PredictionModel predictionModel;
    private Classifier classifier;
    
    /**
     * @param detectorUuid    Detector UUID.
     * @param predictionModel Prediction model.
     * @param classifier      Classification model.
     */
    @JsonCreator
    public AquilaModel(
            @JsonProperty("detectorUuid") UUID detectorUuid,
            @JsonProperty("predictionModel") PredictionModel predictionModel,
            @JsonProperty("classifier") Classifier classifier) {
        
        notNull(detectorUuid, "detectorUuid can't be null");
        notNull(predictionModel, "predictionModel can't be null");
        notNull(classifier, "classifer can't be null");
        
        this.detectorUuid = detectorUuid;
        this.predictionModel = predictionModel;
        this.classifier = classifier;
    }
    
    public AquilaResponse classify(AquilaRequest aquilaRequest) {
        notNull(aquilaRequest, "detectorRequest can't be null");
        
        final Instant instant = Instant.ofEpochSecond(aquilaRequest.getEpochSecond());
        final double observed = aquilaRequest.getObserved();
        final Prediction prediction = predictionModel.predict(instant);
        final Classification classification = classifier.classify(prediction, observed);
        return toAquilaResponse(prediction, classification);
    }
    
    private AquilaResponse toAquilaResponse(Prediction prediction, Classification classification) {
        final double mean = prediction.getMean();
        final double stdev = prediction.getStdev();
        final double weakThreshold = classification.getWeakSigmas() * stdev;
        final double strongThreshold = classification.getStrongSigmas() * stdev;
        
        final AnomalyThresholds thresholds = new AnomalyThresholds(
                mean + strongThreshold,
                mean + weakThreshold,
                mean - strongThreshold,
                mean - weakThreshold);
        
        return new AquilaResponse(mean, thresholds, classification.getAnomalyLevel());
    }
}
