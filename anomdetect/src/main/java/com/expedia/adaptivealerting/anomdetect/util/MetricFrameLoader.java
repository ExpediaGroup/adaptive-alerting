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
package com.expedia.adaptivealerting.anomdetect.util;

import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.jackson.MetricsJavaModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReaderBuilder;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

/**
 * Utility methods for loading metric frames from various sources.
 */
@UtilityClass
@Slf4j
public class MetricFrameLoader {
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new MetricsJavaModule());

    public static MetricFrame loadCsv(File metricDefFile, File metricDataFile, boolean hasHeader)
            throws IOException {

        val metricDef = OBJECT_MAPPER.readValue(metricDefFile, MetricDefinition.class);
        FileInputStream fileInputStream  = new FileInputStream(metricDataFile);
        try {
            return loadCsv(metricDef, fileInputStream, hasHeader);
        } finally {
            if (fileInputStream != null) {
                safeClose(fileInputStream);
            }
        }
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
        val epochSecond = Instant.parse(row[0]).getEpochSecond();
        val value = Float.parseFloat(row[1]);
        return new MetricData(metricDefinition, value, epochSecond);
    }

    private static void safeClose(FileInputStream fis) {
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e) {
                log.error("Error while closing file stream:{}", e);
            }
        }
    }
}
