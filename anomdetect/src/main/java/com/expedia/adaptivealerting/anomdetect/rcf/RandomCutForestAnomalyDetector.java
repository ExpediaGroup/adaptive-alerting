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
package com.expedia.adaptivealerting.anomdetect.rcf;

import com.amazonaws.services.sagemakerruntime.AmazonSageMakerRuntime;
import com.amazonaws.services.sagemakerruntime.AmazonSageMakerRuntimeClientBuilder;
import com.amazonaws.services.sagemakerruntime.model.InvokeEndpointRequest;
import com.amazonaws.services.sagemakerruntime.model.InvokeEndpointResult;
import com.expedia.adaptivealerting.anomdetect.AnomalyDetector;
import com.expedia.adaptivealerting.anomdetect.util.ModelResource;
import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.metrics.MetricData;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * <p>
 * Anomaly detector based on the Random Cut Forest (RCF) algorithm provided by AWS Sagemaker.
 * </p>
 * <p>
 * Invokes endpoint in AWS needs get an anomaly score for metric data. The received score is the likeliness of a metric
 * point to be an anomaly. A metric point is likely an anomaly if it's value is higher than score_cutoff, usually set as
 * score_cutoff = score_mean + 3 * score_std
 * </p>
 * <p>
 * This detector is using {@link Shingle} to obtain better results with the RCF algorithm. To do predictions, the shingle
 * needs to be at full capacity . At start, metric data sent will just fill the shingle. Anomaly scoring is executed
 * after the shingle queue is fully filled.
 * </p>
 *
 * <p>
 * See: https://aws.amazon.com/blogs/machine-learning/use-the-built-in-amazon-sagemaker-random-cut-forest-algorithm-for-anomaly-detection/
 *
 * @author Tatjana Kamenov
 */
@Data
@ToString
@RequiredArgsConstructor
@Slf4j
public final class RandomCutForestAnomalyDetector implements AnomalyDetector {
    private static final String CONTENT_TYPE = "text/csv";
    private static final String ACCEPT = "application/json";

    @NonNull
    private ObjectMapper objectMapper = new ObjectMapper();

    @NonNull
    private UUID uuid;
    private ModelParameters modelParameters;

    private final Shingle shingle;
    private final AmazonSageMakerRuntime amazonSageMaker;
    private final InvokeEndpointRequest invokeEndpointRequest;

    /**
     * Creates a new RCF anomaly detector based on uuid and model parameters.
     *
     * @param uuid UUID The detector uuid.
     * @param modelResource Model parameters containing AWS endpoint and region, shingle size, score cutoffs
     */

    public RandomCutForestAnomalyDetector(UUID uuid, ModelResource modelResource) {
        notNull(uuid, "uuid can't be null");
        log.info("Creating new RandomCutForestAnomalyDetector for uuid {} and modelResource {} ", uuid, modelResource);
        this.uuid = uuid;

        this.modelParameters = new ObjectMapper().convertValue(modelResource.getParams(), ModelParameters.class);

        this.shingle = new Shingle(modelParameters.getShingleSize());
        this.amazonSageMaker = AmazonSageMakerRuntimeClientBuilder.standard().withRegion(modelParameters.getAwsRegion()).build();
        this.invokeEndpointRequest = new InvokeEndpointRequest();
        this.invokeEndpointRequest.setContentType(CONTENT_TYPE);
    }

    /**
     * Classifies metric data into NORMAL, WEAK, STRONG or UNKNOWN class. If shingle is not ready yet (needs
     * to be full to successfully classify), sets the anomaly class to UNKNOWN.
     *
     * @param metricData Incoming metric data.
     *
     * @return AnomalyResult for metric data
     */
    @Override
    public AnomalyResult classify(MetricData metricData) {
        notNull(metricData, "metricData can't be null");

        this.shingle.offer(metricData);

        AnomalyLevel level = AnomalyLevel.UNKNOWN;
        final float weakScoreCutoff = modelParameters.getWeakScoreCutoff();
        final float strongScoreCutoff = modelParameters.getStrongScoreCutoff();

        if (this.shingle.isReady()) {
            final double anomalyScore = getAnomalyScore();
            if (anomalyScore < weakScoreCutoff) {
                level = AnomalyLevel.NORMAL;
            } else if (anomalyScore < strongScoreCutoff) {
                level = AnomalyLevel.WEAK;
            } else {
                level = AnomalyLevel.STRONG;
            }
        }
        return new AnomalyResult(uuid, metricData, level);
    }

    /**
     * Invokes the endpoint with the data that is held in the Shingle queue. This version supports sending one shingle
     * at a time to predict endpoint and getting a single score back.
     **
     * @return AWS anomaly score(s) as a double
     */
    private double getAnomalyScore() {
        // TODO Note: Future versions can easily support sending multiple shingles in one request and getting multiple
        // scores back by simple updates to this class. [TK]
        final String shingleBody = this.shingle.toCsv().get();
        final Optional<ByteBuffer> bodyBuffer =
                Optional.of(ByteBuffer.wrap(shingleBody.getBytes(StandardCharsets.UTF_8)));

        if (bodyBuffer.isPresent()) {
            invokeEndpointRequest.setBody(bodyBuffer.get());
            invokeEndpointRequest.setEndpointName(modelParameters.getEndpoint());
            invokeEndpointRequest.setAccept(ACCEPT);

            final InvokeEndpointResult invokeEndpointResult = amazonSageMaker.invokeEndpoint(invokeEndpointRequest);
            final String bodyResponse = new String(invokeEndpointResult.getBody().array(), StandardCharsets.UTF_8);

            log.info("The RCF score using detector having uuid {} is {}", this.uuid, bodyResponse);

            try {
                final Scores response = objectMapper.readValue(bodyResponse, Scores.class);
                return response.getScores().get(0).getScore();
            } catch (IOException e) {
                log.error("Error deserialising result from AWS endpoint for detector with uuid {}", uuid);
            }
        }
        return -1;
    }

}
