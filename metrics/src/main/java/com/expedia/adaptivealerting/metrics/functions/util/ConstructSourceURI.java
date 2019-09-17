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
package com.expedia.adaptivealerting.metrics.functions.util;

import com.expedia.adaptivealerting.metrics.functions.source.MetricFunctionsSpec;
import com.typesafe.config.Config;

public class ConstructSourceURI {
    private final String GRAPHITE_HOST_KEY = "graphite-host";
    private final String GRAPHITE_RENDER_API_DEFINITION = "/render?target=";
    private final String GRAPHITE_FROM_TIME_PARAM_STRING = "from=-";
    private final String GRAPHITE_TO_TIME_PARAM_STRING = "until=now";
    private final String GRAPHITE_FORMAT_PARAM_STRING = "format=json";
    private final String GRAPHITE_TIME_UNIT = "s";
    private final String URI_CONSTRUCT_AMPERSAND = "&";

    public String getGraphiteURI(Config metricSourceSinkConfig, MetricFunctionsSpec metricFunctionsSpec) {
        String URI = metricSourceSinkConfig.getString(GRAPHITE_HOST_KEY)
                + GRAPHITE_RENDER_API_DEFINITION
                + metricFunctionsSpec.getFunction()
                + URI_CONSTRUCT_AMPERSAND
                + GRAPHITE_FROM_TIME_PARAM_STRING
                + metricFunctionsSpec.getIntervalInSecs() + GRAPHITE_TIME_UNIT
                + URI_CONSTRUCT_AMPERSAND
                + GRAPHITE_TO_TIME_PARAM_STRING
                + URI_CONSTRUCT_AMPERSAND
                + GRAPHITE_FORMAT_PARAM_STRING;
        return URI;
    }

}
