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
    private static final int MINUTES_PER_DAY = 24 * 60;

    /**
     * Client to load metric data from graphite.
     */
    @NonNull
    private GraphiteClient graphiteClient;

    @Override
    public List<DataSourceResult> getMetricData(int totalNoOfDays, int binSize, String metric) {
        List<DataSourceResult> results = new ArrayList<>();
        int maxDataPoints = getMaxDataPointsPerDay(binSize);

        for (int i = 0; i < totalNoOfDays; i++) {
            int from = totalNoOfDays - i;
            int until = from - 1;
            List<GraphiteResult> graphiteResults = graphiteClient.getData(from, until, maxDataPoints, metric);
            if (graphiteResults.size() > 0) {
                String[][] dataPoints = graphiteResults.get(0).getDatapoints();
                //TODO Convert this to use JAVA stream
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
        }
        return results;
    }

    private int getMaxDataPointsPerDay(int binSize) {
        return MINUTES_PER_DAY / binSize;
    }
}