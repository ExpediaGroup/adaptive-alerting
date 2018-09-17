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
import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.metrics.MetricData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;

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
 * To get a prediction if a metric point is an anomaly, endpoint in AWS needs to be invoked. It sends back score which
 * is the likeliness of a metric point to be an anomaly. A metric point is likely an anomaly if it's value is higher
 * than score_cutoff, usually set as score_cutoff = score_mean + 3 * score_std
 * </p>
 * <p>
 * This detector is using shingles to obtain better results with the RCF algorithm. The shingles implementation is in
 * {@link Shingle}. To do predictions, the shingle needs to be full. At start, metric data sent will just fill the
 * shingle. Anomaly scoring is executed after the queue is fully filled.
 * </p>
 *
 * <p>
 * See: https://aws.amazon.com/blogs/machine-learning/use-the-built-in-amazon-sagemaker-random-cut-forest-algorithm-for-anomaly-detection/
 *
 * @author Tatjana Kamenov
 */
@Data
@ToString
public final class RandomCutForestAnomalyDetector implements AnomalyDetector {
    private static final String TEXT_CSV_CONTENT_TYPE = "text/csv";
    private static final String APPLICATION_JSON_ACCEPT = "application/json";

    private static final String AWS_REGION = PropertiesCache.getInstance().get("aws_region");
    private static final String ENDPOINT = PropertiesCache.getInstance().get("sagemaker_endpoint");
    private static final int SHINGLE_SIZE = Integer.valueOf(PropertiesCache.getInstance().get("shingle_size"));
    private static final double STRONG_SCORE_CUTOFF = Double.valueOf(PropertiesCache.getInstance().get("strong_score_cutoff"));
    private static final double WEAK_SCORE_CUTOFF = Double.valueOf(PropertiesCache.getInstance().get("weak_score_cutoff"));
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    @NonNull
    private UUID uuid;
    
    private final Shingle shingle;
    private final AmazonSageMakerRuntime amazonSageMaker;
    private final InvokeEndpointRequest invokeEndpointRequest;

    // TODO Consider passing endpoint, shingle size and score cutoff to constructor [TK]
    // - If we do this, please create a Params class for these. See some of the other detectors. [WLW]
    public RandomCutForestAnomalyDetector(UUID uuid) {
        notNull(uuid, "uuid can't be null");
        
        this.uuid = uuid;
        this.shingle = new Shingle(SHINGLE_SIZE);
        this.amazonSageMaker = AmazonSageMakerRuntimeClientBuilder.standard().withRegion(AWS_REGION).build();
        this.invokeEndpointRequest = new InvokeEndpointRequest();
        this.invokeEndpointRequest.setContentType(TEXT_CSV_CONTENT_TYPE);
    }
    
    @Override
    public AnomalyResult classify(MetricData metricData) {
        notNull(metricData, "metricData can't be null");
        
        this.shingle.offer(metricData);
        
        AnomalyLevel level = AnomalyLevel.UNKNOWN;
        if (this.shingle.isReady()) {
            final double anomalyScore = getAnomalyScore();
            if (anomalyScore < WEAK_SCORE_CUTOFF) {
                level = AnomalyLevel.NORMAL;
            } else if (anomalyScore < STRONG_SCORE_CUTOFF) {
                level = AnomalyLevel.WEAK;
            } else {
                level = AnomalyLevel.STRONG;
            }
        }
        return new AnomalyResult(uuid, metricData, level);
    }

    /**
     * Invokes the endpoint with the data that is held in the Shingle queue.
     * @return AWS anomaly score as a double
     */
    private double getAnomalyScore() {
        final String shingleBody = this.shingle.toCsv().get();
        final Optional<ByteBuffer> bodyBuffer =
                Optional.of(ByteBuffer.wrap(shingleBody.getBytes(StandardCharsets.UTF_8)));

        if (bodyBuffer.isPresent()) {
            invokeEndpointRequest.setBody(bodyBuffer.get());
            invokeEndpointRequest.setEndpointName(ENDPOINT);
            invokeEndpointRequest.setAccept(APPLICATION_JSON_ACCEPT);

            final InvokeEndpointResult invokeEndpointResult = amazonSageMaker.invokeEndpoint(invokeEndpointRequest);
            final String bodyResponse = new String(invokeEndpointResult.getBody().array(), StandardCharsets.UTF_8);

            try {
                final Scores response = OBJECT_MAPPER.readValue(bodyResponse, Scores.class);
                return response.getScores().get(0).getScore();
            } catch (IOException e) {
                throw new RandomCutForestProcessingException("Error deserialising result from AWS inference endpoint", e);
            }
        }
        return -1;
    }
}
