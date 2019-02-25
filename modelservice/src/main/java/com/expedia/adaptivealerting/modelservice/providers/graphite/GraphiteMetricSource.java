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
package com.expedia.adaptivealerting.modelservice.providers.graphite;

import com.expedia.adaptivealerting.modelservice.spi.MetricSource;
import com.expedia.adaptivealerting.modelservice.spi.MetricSourceResult;
import com.expedia.adaptivealerting.modelservice.util.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Graphite metric source
 */
@Slf4j
@Service
@Configurable
public class GraphiteMetricSource implements MetricSource {

    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public List<MetricSourceResult> getMetricData(String metricName) {
        GraphiteProperties props = BeanUtil.getBean(GraphiteProperties.class);
        GraphiteRequest request = new GraphiteRequest("karmalab.stats.gauges.airsupply.shopping.search.cheiassint002.metrics.jvm.memory.total.used.Value");
        Map<String, Object> params = request.toParams();

        GraphiteResult graphiteResult = restTemplate.getForObject(props.getUrlTemplate(), GraphiteResult[].class, params)[0];
        String[][] dataPoints = graphiteResult.getDatapoints();
        List<MetricSourceResult> results = new ArrayList<>();
        for (String[] dataPoint : dataPoints) {
            Double dataPointValue = Double.parseDouble(dataPoint[0]);
            long epochSeconds = Long.parseLong(dataPoint[1]);
            MetricSourceResult result = new MetricSourceResult(dataPointValue, epochSeconds);
            results.add(result);
        }
        return results;
    }
}