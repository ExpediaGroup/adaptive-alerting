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
package com.expedia.aquila.detect;

import com.expedia.adaptivealerting.anomdetect.AnomalyDetector;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.data.MappedMpoint;
import com.expedia.adaptivealerting.core.data.Mpoint;
import com.expedia.aquila.core.model.Classification;
import com.expedia.aquila.core.model.Classifier;
import com.expedia.aquila.core.model.Prediction;
import com.expedia.aquila.core.model.PredictionModel;
import com.expedia.aquila.core.util.MetricUtil;
import com.expedia.www.haystack.commons.entities.MetricPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * AquilaAnomalyDetector anomaly detector.
 *
 * @author Willie Wheeler
 * @author Karan Shah
 */
public final class AquilaAnomalyDetector implements AnomalyDetector {
    private static final Logger log = LoggerFactory.getLogger(AquilaAnomalyDetector.class);
    
    private UUID uuid;
    private PredictionModel predictionModel;
    private Classifier classifier;
    
    /**
     * @param predictionModel Prediction model.
     */
    public AquilaAnomalyDetector(PredictionModel predictionModel) {
        notNull(predictionModel, "predictionModel can't be null");
        this.predictionModel = predictionModel;
        this.classifier = new Classifier();
    }
    
    public UUID getUuid() {
        return uuid;
    }
    
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
    
    public PredictionModel getPredictionModel() {
        return predictionModel;
    }
    
    public Classifier getClassifier() {
        return classifier;
    }
    
    @Override
    @Deprecated
    public AnomalyResult classify(MetricPoint metricPoint) {
        final MappedMpoint mappedMpoint = new MappedMpoint();
        mappedMpoint.setMpoint(MetricUtil.toMpoint(metricPoint));
        return classify(mappedMpoint).getAnomalyResult();
    }
    
    @Override
    public MappedMpoint classify(MappedMpoint mappedMpoint) {
        notNull(mappedMpoint, "mappedMpoint can't be null");
        final Mpoint mpoint = mappedMpoint.getMpoint();
        final Prediction prediction = predictionModel.predict(Instant.ofEpochSecond(mpoint.getEpochTimeInSeconds()));
        final Classification classification = classifier.classify(mpoint, prediction);
        final AnomalyResult result = MetricUtil.toAnomalyResult(mpoint, prediction, classification);
        mappedMpoint.setAnomalyResult(result);
        return mappedMpoint;
    }
}
