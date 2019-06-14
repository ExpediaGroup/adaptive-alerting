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
import lombok.val;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Utility methods for loading metric frames from various sources.
 */
@UtilityClass
public class MetricFrameLoader {
    private static final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new MetricsJavaModule());

    public static MetricFrame loadCsv(File metricDefFile, File metricDataFile, boolean hasHeader)
            throws IOException, ParseException {

        val metricDef = OBJECT_MAPPER.readValue(metricDefFile, MetricDefinition.class);
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
            throws IOException, ParseException {

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

    private static MetricData toMetricData(MetricDefinition metricDefinition, String[] row)
            throws ParseException {

        long epochSecond = DATE_TIME_FORMAT.parse(row[0]).toInstant().getEpochSecond();
        return new MetricData(metricDefinition, Float.parseFloat(row[1]), epochSecond);
    }
}
