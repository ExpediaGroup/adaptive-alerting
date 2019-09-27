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
package com.expedia.adaptivealerting.metrics.functions;

import com.expedia.adaptivealerting.anomdetect.util.HttpClientWrapper;
import com.expedia.adaptivealerting.metrics.functions.service.graphite.GraphiteQueryService;
import com.expedia.adaptivealerting.metrics.functions.sink.MetricFunctionsPublish;
import com.expedia.adaptivealerting.metrics.functions.source.MetricFunctionsSpec;
import com.expedia.metrics.MetricData;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetricFunctionsTask implements Runnable {
    private MetricFunctionsSpec metricFunctionsSpec;
    private MetricFunctionsPublish metricFunctionsPublish;
    private Config metricSourceSinkConfig;


    public MetricFunctionsTask (MetricFunctionsSpec metricFunctionsSpec, MetricFunctionsPublish metricFunctionsPublish,
                                Config metricSourceSinkConfig) {
        this.metricFunctionsSpec = metricFunctionsSpec;
        this.metricFunctionsPublish = metricFunctionsPublish;
        this.metricSourceSinkConfig = metricSourceSinkConfig;
    }

    @SuppressWarnings("unchecked")
    public void run() {
        HttpClientWrapper httpClientWrapper = new HttpClientWrapper();
        GraphiteQueryService graphiteQueryService = new GraphiteQueryService(httpClientWrapper);
        try {
            MetricData metricData = graphiteQueryService.queryMetricSource(metricSourceSinkConfig,
                    metricFunctionsSpec);
            metricFunctionsPublish.publishMetrics(metricData);
        }
        catch (Exception e) {
            log.error("Exception while processing metrics function", e);

        }
    }

}