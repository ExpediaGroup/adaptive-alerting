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
package com.expedia.adaptivealerting.anomdetect.randomcutforest;

import com.amazonaws.services.sagemakerruntime.AmazonSageMakerRuntime;
import com.amazonaws.services.sagemakerruntime.AmazonSageMakerRuntimeClientBuilder;
import com.amazonaws.services.sagemakerruntime.model.InvokeEndpointRequest;
import com.amazonaws.services.sagemakerruntime.model.InvokeEndpointResult;
import com.expedia.adaptivealerting.anomdetect.AnomalyDetector;
import com.expedia.adaptivealerting.anomdetect.randomcutforest.beans.Scores;
import com.expedia.adaptivealerting.anomdetect.randomcutforest.util.PropertiesCache;
import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.data.MappedMpoint;
import com.expedia.adaptivealerting.core.data.Mpoint;
import com.expedia.adaptivealerting.core.util.AssertUtil;
import com.expedia.adaptivealerting.core.util.MetricUtil;
import com.expedia.www.haystack.commons.entities.MetricPoint;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * <p>
 * Anomaly detector based on the Random Cut Forest (RCF) algorithm provided by AWS Sagemaker.
 * </p>
 * <p>
 * To get a prediction if a metric point is an anomaly, endpoint in AWS needs to be invoked.
 * It sends back score which is the likeliness of a metric point to be an anomaly.
 * A metric point is likely an anomaly if it's value is higher than score_cutoff, usually set as score_cutoff = score_mean + 3 * score_std
 * </p>
 * <p>
 * This detector is using shingles to obtain better results with the RCF algoritm. The shingles implementation is in the
 * MetricPointQueue. To do predictions, the MetricPointQueue needs to be full. At start, MetricPoints sent will just
 * fill the MetricPointQueue. Anomaly scoring is executed after the queue is fully filled.
 * </p>
 *
 * <p>
 * See: https://aws.amazon.com/blogs/machine-learning/use-the-built-in-amazon-sagemaker-random-cut-forest-algorithm-for-anomaly-detection/
 *
 * @author Tatjana Kamenov
 */
public class RandomCutForestAnomalyDetector implements AnomalyDetector {

    private static final String TEXT_CSV_CONTENT_TYPE = "text/csv";
    private static final String APPLICATION_JSON_ACCEPT = "application/json";

    private static final String AWS_REGION = PropertiesCache.getInstance().get("aws_region");
    private static final String ENDPOINT = PropertiesCache.getInstance().get("sagemaker_endpoint");
    private static final int SHINGLE_SIZE = Integer.valueOf(PropertiesCache.getInstance().get("shingle_size"));
    private static final double STRONG_SCORE_CUTOFF = Double.valueOf(PropertiesCache.getInstance().get("strong_score_cutoff"));
    private static final double WEAK_SCORE_CUTOFF = Double.valueOf(PropertiesCache.getInstance().get("weak_score_cutoff"));

    private final Shingle shingle;
    private final AmazonSageMakerRuntime amazonSageMaker;
    private final InvokeEndpointRequest invokeEndpointRequest;
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    //TODO Consider passing endpoint, shingle size and score cutoff to constructor
    public RandomCutForestAnomalyDetector() {
        this.shingle = new Shingle(SHINGLE_SIZE);
        this.amazonSageMaker = AmazonSageMakerRuntimeClientBuilder.standard().withRegion(AWS_REGION).build();
        this.invokeEndpointRequest = new InvokeEndpointRequest();
        this.invokeEndpointRequest.setContentType(TEXT_CSV_CONTENT_TYPE);
    }

    /**
     * Determine if MetricPoint is an anomaly.
     * @param metricPoint Metric point to analyse.
     * @return AnomalyResult based on score received from Sagemaker
     */
    @Override
    @Deprecated
    public AnomalyResult classify(MetricPoint metricPoint) {
        final MappedMpoint mappedMpoint = new MappedMpoint();
        mappedMpoint.setMpoint(MetricUtil.toMpoint(metricPoint));
        return classify(mappedMpoint).getAnomalyResult();
    }
    
    @Override
    public MappedMpoint classify(MappedMpoint mappedMpoint) {
        AssertUtil.notNull(mappedMpoint, "metricPoint can't be null");

        this.shingle.offer(mappedMpoint);

        final AnomalyResult anomalyResult = new AnomalyResult();
        if (this.shingle.isReady()) {
            final double anomalyScore = getAnomalyScore();
            anomalyResult.setEpochSecond(mappedMpoint.getMpoint().getEpochTimeInSeconds());
            anomalyResult.setAnomalyScore(anomalyScore);
            if (anomalyScore < WEAK_SCORE_CUTOFF) {
                anomalyResult.setAnomalyLevel(AnomalyLevel.NORMAL);
            } else if (anomalyScore < STRONG_SCORE_CUTOFF) {
                anomalyResult.setAnomalyLevel(AnomalyLevel.WEAK);
            } else {
                anomalyResult.setAnomalyLevel(AnomalyLevel.STRONG);
            }
        }
        final MappedMpoint result = new MappedMpoint();
        result.setAnomalyResult(anomalyResult);
        result.setDetectorType(mappedMpoint.getDetectorType());
        result.setDetectorUuid(mappedMpoint.getDetectorUuid());
        result.setMpoint(mappedMpoint.getMpoint());

        return result;
    }

    /**
     * Invokes the endpoint with the data that is held in the Shingle queue.
     * @return AWS anomaly score as a double
     */
    private double getAnomalyScore() {

        final String shingleBody = this.shingle.toCsv().get();

        final Optional<ByteBuffer> bodyBuffer = Optional.of(ByteBuffer.wrap(shingleBody.getBytes(StandardCharsets.UTF_8)));

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
