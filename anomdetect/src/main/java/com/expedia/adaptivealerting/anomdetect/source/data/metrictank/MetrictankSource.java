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
package com.expedia.adaptivealerting.anomdetect.source.data.metrictank;

import com.expedia.adaptivealerting.anomdetect.source.data.DataSource;
import com.expedia.adaptivealerting.anomdetect.source.data.DataSourceResult;
import com.expedia.adaptivealerting.anomdetect.util.TimeConstantsUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static java.time.Instant.ofEpochSecond;

@RequiredArgsConstructor
@Slf4j
public class MetrictankSource implements DataSource {

    public static final Double MISSING_VALUE = Double.NEGATIVE_INFINITY;

    /**
     * Client to load metric data from graphite.
     */
    @NonNull
    private MetrictankClient metricTankClient;

    @Override
    public List<DataSourceResult> getMetricData(long earliestTime, long latestTime, int intervalLength, String target) {
        return buildDataSourceResult(earliestTime, latestTime, target);
    }

    private List<DataSourceResult> buildDataSourceResult(long earliestTime, long latestTime, String metric) {
        List<DataSourceResult> results = new ArrayList<>();
        for (long i = earliestTime; i < latestTime; i += TimeConstantsUtil.SECONDS_PER_DAY) {
            List<MetrictankResult> metrictankResults = getDataFromGraphite(i, metric);
            if (metrictankResults.size() > 0) {
                String[][] dataPoints = metrictankResults.get(0).getDatapoints();
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
        logResults(results);
        return results;
    }

    private List<MetrictankResult> getDataFromGraphite(long from, String metric) {
        long until = from + TimeConstantsUtil.SECONDS_PER_DAY;
        log.debug("Querying Metrictank with: from={} ({}), until={} ({}), metric='{}'",
                from, ofEpochSecond(from), until, ofEpochSecond(until), metric);
        return metricTankClient.getData(from, until, metric);
    }

    private void logResults(List<DataSourceResult> results) {
        if (!results.isEmpty()) {
            DataSourceResult firstResult = results.get(0);
            DataSourceResult lastResult = results.get(results.size() - 1);
            long actualFrom = firstResult.getEpochSecond();
            long actualUntil = lastResult.getEpochSecond();
            log.debug(String.format("Retrieved %d data points from Graphite from %d (%s) until %d (%s)",
                    results.size(),
                    actualFrom,
                    ofEpochSecond(actualFrom).toString(),
                    actualUntil,
                    ofEpochSecond(actualUntil).toString()));
        }
    }

}