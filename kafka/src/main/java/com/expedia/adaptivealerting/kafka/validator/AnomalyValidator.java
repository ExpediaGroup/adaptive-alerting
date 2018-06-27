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
package com.expedia.adaptivealerting.kafka.validator;

import com.expedia.adaptivealerting.anomvalidate.filter.InvestigationFilter;
import com.expedia.adaptivealerting.anomvalidate.filter.PostInvestigationFilter;
import com.expedia.adaptivealerting.anomvalidate.filter.PreInvestigationFilter;
import com.expedia.adaptivealerting.anomvalidate.investigation.InvestigationManager;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.kafka.util.AppUtil;
import com.expedia.adaptivealerting.kafka.util.BaseStreamRunnerBuilder;
import com.expedia.www.haystack.commons.kstreams.app.StreamsRunner;
import com.typesafe.config.Config;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;


public class AnomalyValidator {

    public static void main(String[] args) {
        Config appConfig = AppUtil.getAppConfig("anomaly-validator");
        AppUtil.launchStreamRunner(new StreamRunnerBuilder().build(appConfig));
    }

    public static class StreamRunnerBuilder extends BaseStreamRunnerBuilder {
        @Override
        public StreamsRunner build(Config appConfig) {
            final StreamsBuilder builder = createStreamsBuilder(appConfig);

            return createStreamsRunner(appConfig, builder);
        }

        private static StreamsBuilder createStreamsBuilder(Config appConfig) {
            final StreamsBuilder builder = new StreamsBuilder();
            final KStream<String, AnomalyResult> anomalies = builder.stream(appConfig.getString("topic"));

            InvestigationFilter preInvestigationFilter = new PreInvestigationFilter();
            InvestigationFilter postInvestigationFilter = new PostInvestigationFilter();

            String endpoint = appConfig.hasPath("investigation.endpoint")
                    ? appConfig.getString("investigation.endpoint") : null;
            Integer timeoutMs = appConfig.hasPath("investigation.timeoutMs")
                    ? appConfig.getInt("investigation.timeoutMs") : null;
            InvestigationManager investigationManager = new InvestigationManager(endpoint, timeoutMs);

            anomalies
                    .filter((k, anomalyResult) -> preInvestigationFilter.keep(anomalyResult))
                    .mapValues(investigationManager::investigate)
                    .filter((k, anomalyResult) -> postInvestigationFilter.keep(anomalyResult))
                    .to("alerts");

            return builder;
        }
    }
}
