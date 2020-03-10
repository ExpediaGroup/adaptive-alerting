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

import com.expedia.adaptivealerting.metrics.functions.service.MetricQueryService;
import com.expedia.adaptivealerting.metrics.functions.service.MetricQueryServiceException;
import com.expedia.adaptivealerting.metrics.functions.sink.MetricFunctionsPublish;
import com.expedia.adaptivealerting.metrics.functions.source.MetricFunctionsSpec;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

@Slf4j
public class MetricFunctionsTask implements Runnable {
    private Config metricStoreConfig;
    private MetricFunctionsSpec spec;
    private MetricFunctionsPublish publisher;
    private MetricQueryService metricQueryService;

    public MetricFunctionsTask(Config metricStoreConfig, MetricFunctionsSpec spec, MetricFunctionsPublish publisher) {
        notNull(metricStoreConfig, "metricStoreConfig can't be null");
        notNull(spec, "spec can't be null");
        notNull(publisher, "publisher can't be null");

        this.metricStoreConfig = metricStoreConfig;
        this.spec = spec;
        this.publisher = publisher;
        this.metricQueryService = new MetricQueryService();
    }

    public void run() {
        try {
            val metricData = metricQueryService.queryMetricSource(metricStoreConfig, spec);
            publisher.publishMetrics(metricData);
        } catch (MetricQueryServiceException metricQueryServiceException) {
            log.error(metricQueryServiceException.getMessage(), metricQueryServiceException.getCause());
        } catch (Exception e) {
            log.error("Unhandled exception while processing metrics function", e);
        }
    }

}