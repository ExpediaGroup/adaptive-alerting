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
package com.expedia.adaptivealerting.anomvalidate.investigation;

import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.anomaly.InvestigationResult;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class InvestigationManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(InvestigationManager.class);
    private static final int DEFAULT_TIMEOUT_MS = 30000;
    private String endpoint;
    private int timeoutMs;
    private final ObjectReader reader = new ObjectMapper().readerFor(new TypeReference<List<InvestigationResult>>() {});
    private final ObjectWriter writer = new ObjectMapper().writerFor(AnomalyResult.class);

    public InvestigationManager(String endpoint, Integer timeoutMs) {
        this.endpoint = endpoint;
        this.timeoutMs = timeoutMs == null ? DEFAULT_TIMEOUT_MS : timeoutMs;
    }

    public MappedMetricData investigate(MappedMetricData mappedMetricData) {
        if (mappedMetricData != null) {
            AnomalyResult anomalyResult = mappedMetricData.getAnomalyResult();
            if (anomalyResult != null) {
                anomalyResult.setInvestigationResults(requestInvestigation(anomalyResult));
                LOGGER.info(
                        "Investigation: investigationResultsSize={} resultLevel={} hashcode={} tags={}",
                        anomalyResult.getInvestigationResults() == null
                                ? 0 : anomalyResult.getInvestigationResults().size(),
                        anomalyResult.getAnomalyLevel(),
                        anomalyResult.getMetricDefinition().getTags().hashCode(),
                        anomalyResult.getMetricDefinition().getTags().toString()
                );
            }
        }
        return mappedMetricData;
    }

    private List<InvestigationResult> requestInvestigation(AnomalyResult result) {
        if (StringUtils.isEmpty(endpoint)) {
            return Collections.emptyList();
        }
        String postData = "";
        try {
            postData = writer.writeValueAsString(result);
            String response = Request.Post(endpoint)
                    .bodyString(postData, ContentType.APPLICATION_JSON)
                    .socketTimeout(timeoutMs)
                    .connectTimeout(timeoutMs) // TODO: have separate timeouts or a total timeout for connection.
                    .execute()
                    .returnContent()
                    .asString();

            return reader.readValue(response);
        } catch (IOException e) {
            LOGGER.error("Error while investigating.", e);
            LOGGER.error("Trying to Post to: " + endpoint);
            LOGGER.error("Data posted: " + postData);
            if (e instanceof HttpResponseException) {
                LOGGER.error("Status Code: " + ((HttpResponseException) e).getStatusCode());
            }
            return Collections.emptyList();
        }
    }
}
