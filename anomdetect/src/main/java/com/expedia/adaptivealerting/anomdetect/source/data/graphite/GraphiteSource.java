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
package com.expedia.adaptivealerting.anomdetect.source.data.graphite;

import com.expedia.adaptivealerting.anomdetect.source.data.DataSource;
import com.expedia.adaptivealerting.anomdetect.source.data.DataSourceResult;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;


@RequiredArgsConstructor
@Slf4j
public class GraphiteSource implements DataSource {

    public static final Double MISSING_VALUE = Double.NEGATIVE_INFINITY;

    /**
     * Client to load metric data from graphite.
     */
    @NonNull
    private GraphiteClient graphiteClient;

    @Override
    public List<DataSourceResult> getMetricData(String from, Integer maxDataPoints, String metric) {
        GraphiteResult[] graphiteResult = graphiteClient.getMetricData(from, maxDataPoints, metric);
        List<DataSourceResult> results = new ArrayList<>();
        if (graphiteResult.length != 0) {
            String[][] dataPoints = graphiteResult[0].getDatapoints();
            for (String[] dataPoint : dataPoints) {
                Double value = MISSING_VALUE;
                if (dataPoint[0] != null) {
                    value = Double.parseDouble(dataPoint[0]);
                }
                long epochSeconds = Long.parseLong(dataPoint[1]);
                DataSourceResult result = new DataSourceResult(value, epochSeconds);
                results.add(result);
            }
        }
        return results;
    }
}
