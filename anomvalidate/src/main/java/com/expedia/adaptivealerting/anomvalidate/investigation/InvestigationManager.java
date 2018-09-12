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
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.TagCollection;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
public class InvestigationManager {
    private static final int DEFAULT_TIMEOUT_MS = 30000;
    private String endpoint;
    private int timeoutMs;
    private final ObjectReader reader = new ObjectMapper().readerFor(new TypeReference<List<InvestigationResult>>() {});
    private final ObjectWriter writer = new ObjectMapper().writerFor(AnomalyResult.class);
    
    public InvestigationManager(String endpoint, Integer timeoutMs) {
        this.endpoint = endpoint;
        this.timeoutMs = timeoutMs == null ? DEFAULT_TIMEOUT_MS : timeoutMs;
    }
    
    // TODO Return InvestigationResults, not MappedMetricData
    public MappedMetricData investigate(MappedMetricData mappedMetricData) {
        if (mappedMetricData != null) {
            final AnomalyResult anomalyResult = mappedMetricData.getAnomalyResult();
            final MetricData metricData = anomalyResult.getMetricData();
            final MetricDefinition metricDefinition = metricData.getMetricDefinition();
            final TagCollection tags = metricDefinition.getTags();
            
            if (anomalyResult != null) {
                anomalyResult.setInvestigationResults(requestInvestigation(anomalyResult));
                log.info(
                        "Investigation: investigationResultsSize={} resultLevel={} hashcode={} tags={}",
                        anomalyResult.getInvestigationResults() == null
                                ? 0 : anomalyResult.getInvestigationResults().size(),
                        anomalyResult.getAnomalyLevel(),
                        tags.hashCode(),
                        tags.toString()
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
            log.error("Error while investigating.", e);
            log.error("Data posted: {}", postData);
            if (e instanceof HttpResponseException) {
                log.error("Status code: {}", ((HttpResponseException) e).getStatusCode());
            }
            return Collections.emptyList();
        }
    }
}
