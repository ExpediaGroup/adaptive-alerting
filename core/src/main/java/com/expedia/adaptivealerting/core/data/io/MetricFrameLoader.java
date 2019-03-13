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
package com.expedia.adaptivealerting.core.data.io;

import com.expedia.adaptivealerting.core.data.MetricFrame;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.jackson.MetricsJavaModule;
import com.expedia.metrics.metrictank.MetricTankIdFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReaderBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

/**
 * Utility methods for loading metric frames from various sources.
 */
@Slf4j
public final class MetricFrameLoader {
    final static ObjectMapper objectMapper = new ObjectMapper().registerModule(new MetricsJavaModule());

    public static MetricFrame loadCsv(File metricDefFile, File metricDataFile, boolean hasHeader)
            throws IOException {

        val metricDef = objectMapper.readValue(metricDefFile, MetricDefinition.class);
        log.info("metricDef={}", metricDef);
        log.info("metricId={}", new MetricTankIdFactory().getId(metricDef));
        return loadCsv(metricDef, new FileInputStream(metricDataFile), hasHeader);
    }

    /**
     * Loads a {@link MetricFrame} from a CSV input stream.
     *
     * @param metricDef The underlying metric.
     * @param in        CSV input stream.
     * @param hasHeader Indicates whether the data has a header row.
     * @return A data frame containing the CSV data.
     * @throws IOException if there's a problem reading the CSV input stream.
     */
    public static MetricFrame loadCsv(MetricDefinition metricDef, InputStream in, boolean hasHeader)
            throws IOException {

        List<String[]> rows;
        try (val br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            val skipLines = hasHeader ? 1 : 0;
            val reader = new CSVReaderBuilder(br).withSkipLines(skipLines).build();
            rows = reader.readAll();
        }

        val numRows = rows.size();
        val metricData = new MetricData[numRows];

        for (int i = 0; i < numRows; i++) {
            metricData[i] = toMetricData(metricDef, rows.get(i));
        }

        return new MetricFrame(metricData);
    }

    private static MetricData toMetricData(MetricDefinition metricDefinition, String[] row) {
        // Some of the CSVs use Instants, some use epoch seconds
        long epochSeconds;
        try {
            epochSeconds = Long.parseLong(row[0]);
        } catch (NumberFormatException nfe) {
            epochSeconds = Instant.parse(row[0]).getEpochSecond();
        }
        return new MetricData(metricDefinition, Float.parseFloat(row[1]), epochSeconds);
    }
}
