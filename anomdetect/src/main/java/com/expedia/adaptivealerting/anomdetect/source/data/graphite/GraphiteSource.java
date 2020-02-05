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
import com.expedia.adaptivealerting.anomdetect.util.C;
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
    public List<DataSourceResult> getMetricData(int earliestTimeInSecs, int binSizeInSecs, String metric) {
        int maxDataPoints = getMaxDataPointsPerDay(binSizeInSecs);
        List<DataSourceResult> results = new ArrayList<>();
        for (int i = 0; i < earliestTimeInSecs; i += C.SECONDS_PER_DAY) {
            List<GraphiteResult> graphiteResults = getDataFromGraphite(i, earliestTimeInSecs, maxDataPoints, metric);
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

    private List<GraphiteResult> getDataFromGraphite(int counter, int earliestTimeInSecs, int maxDataPoints, String metric) {
        int from = earliestTimeInSecs - counter;
        int until = from - C.SECONDS_PER_DAY;
        log.debug("Fetching data from graphite for params:" +
                "from={}, until={}, maxDataPoints={} and metric={} ", from, until, maxDataPoints, metric);
        return graphiteClient.getData(from, until, maxDataPoints, metric);
    }

    private int getMaxDataPointsPerDay(int binSizeInSecs) {
        return C.SECONDS_PER_DAY / binSizeInSecs;
    }
}