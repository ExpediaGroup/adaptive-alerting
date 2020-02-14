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
import com.expedia.adaptivealerting.anomdetect.util.DateUtil;
import com.expedia.adaptivealerting.anomdetect.util.TimeConstantsUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static java.time.Instant.ofEpochSecond;

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
    public List<DataSourceResult> getMetricData(long earliestTime, long latestTime, int intervalLength, String target) {
        return buildDataSourceResult(earliestTime, latestTime, target, intervalLength);
    }

    private List<DataSourceResult> buildDataSourceResult(long earliestTime, long latestTime, String metric, int intervalLength) {
        List<DataSourceResult> results = new ArrayList<>();
        long earliestTimeSnappedToInterval = epochTimeSnappedToSeconds(earliestTime, intervalLength);
        long latestTimeSnappedToInterval = epochTimeSnappedToSeconds(latestTime, intervalLength);

        for (long i = earliestTimeSnappedToInterval; i < latestTimeSnappedToInterval; i += TimeConstantsUtil.SECONDS_PER_DAY) {
            List<GraphiteResult> graphiteResults = getDataFromGraphite(i, metric, intervalLength);

            if (graphiteResults.size() > 0) {
                String[][] dataPoints = graphiteResults.get(0).getDatapoints();
                //TODO Convert this to use JAVA stream
                // We discard the last data point to ensure current bin is not included in Graphite data retrieval.
                for (int j = 0; j < dataPoints.length - 1; j++) {
                    Double value = MISSING_VALUE;
                    if (dataPoints[j][0] != null) {
                        value = Double.parseDouble(dataPoints[j][0]);
                    }
                    long epochSeconds = Long.parseLong(dataPoints[j][1]);
                    DataSourceResult result = new DataSourceResult(value, epochSeconds);
                    results.add(result);
                }
            }
        }
        logResults(results);
        return results;
    }

    private List<GraphiteResult> getDataFromGraphite(long from, String metric, int intervalLength) {
        long until = from + TimeConstantsUtil.SECONDS_PER_DAY;
        log.debug("Querying Graphite with: from={} ({}), until={} ({}), metric='{}'",
                from, ofEpochSecond(from), until, ofEpochSecond(until), metric);
        return graphiteClient.getData(from, until, metric);
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

    private long epochTimeSnappedToSeconds(long time, int seconds) {
        return DateUtil.snapToSeconds(ofEpochSecond(time), seconds).getEpochSecond();
    }
}
