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
package com.expedia.adaptivealerting.modelservice.spi;

import com.expedia.adaptivealerting.modelservice.spi.graphite.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * @author kashah
 */
@Slf4j
@Service
@Configurable
public class GraphiteMetricSource implements MetricSource {

    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public List<MetricSourceResult> getMetricData(String metricName) {
        GraphiteRequest request = new GraphiteRequest(metricName);
        Map<String, Object> params = request.toParams();
        GraphiteResult graphiteResult = restTemplate.getForObject(getGraphiteUrl(), GraphiteResult[].class, params)[0];
        String[][] dataPoints = graphiteResult.getDatapoints();
        List<MetricSourceResult> results = new ArrayList<>();
        for (String[] dataPoint : dataPoints) {
            Double dataPointValue = Double.parseDouble(dataPoint[0]);
            Long epochSeconds = Long.parseLong(dataPoint[1]);
            MetricSourceResult result = new MetricSourceResult(dataPointValue, epochSeconds);
            results.add(result);
        }
        return results;
    }

    private String getGraphiteUrl() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        GraphiteProperties properties = null;
        try {
            properties = mapper.readValue(new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/application.yml"))), GraphiteProperties.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties.getGraphite().get("urlTemplate").textValue();
    }
}