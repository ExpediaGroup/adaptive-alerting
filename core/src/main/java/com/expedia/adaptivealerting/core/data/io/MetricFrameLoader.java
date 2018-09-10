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
package com.expedia.adaptivealerting.core.data.io;

import com.expedia.adaptivealerting.core.data.MetricFrame;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

/**
 * Utility methods for loading metric frames from various sources.
 *
 * @author Willie Wheeler
 */
public final class MetricFrameLoader {
    
    /**
     * Loads a {@link MetricFrame} from a CSV file. Assumes that the file has timestamps.
     *
     * @param metric    The underlying metric.
     * @param file      CSV file.
     * @param hasHeader Indicates whether the data has a header row.
     * @return A data frame containing the CSV data.
     * @throws IOException if there's a problem reading the CSV file.
     */
    public static MetricFrame loadCsv(MetricDefinition metric, File file, boolean hasHeader) throws IOException {
        try (final InputStream is = new FileInputStream(file)) {
            return loadCsv(metric, is, hasHeader);
        }
    }
    
    /**
     * Loads a {@link MetricFrame} from a CSV input stream.
     *
     * @param metric    The underlying metric.
     * @param in        CSV input stream.
     * @param hasHeader Indicates whether the data has a header row.
     * @return A data frame containing the CSV data.
     * @throws IOException if there's a problem reading the CSV input stream.
     */
    public static MetricFrame loadCsv(MetricDefinition metric, InputStream in, boolean hasHeader) throws IOException {
        List<String[]> rows;
        try (final BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            final int skipLines = hasHeader ? 1 : 0;
            final CSVReader reader = new CSVReaderBuilder(br).withSkipLines(skipLines).build();
            rows = reader.readAll();
        }
        
        final int numRows = rows.size();
        final MetricData[] mpoints = new MetricData[numRows];
        
        for (int i = 0; i < numRows; i++) {
            mpoints[i] = toMetricData(metric, rows.get(i));
        }
        return new MetricFrame(mpoints);
    }
    
    /**
     * Loader for CSV files with missing timestamps.
     *
     * @param metric            The underlying metric.
     * @param in                CSV input stream.
     * @param hasHeader         Indicates whether the data has a header row.
     * @param startDate         Start date.
     * @param intervalInMinutes Interval in minutes.
     * @return A data frame containing the CSV data.
     * @throws IOException if there's a problem reading the CSV input stream.
     */
    public static MetricFrame loadCsvMissingTimestamps(
            MetricDefinition metric,
            InputStream in,
            boolean hasHeader,
            Instant startDate,
            int intervalInMinutes)
            throws IOException {
        
        List<String[]> rows;
        try (final BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            final int skipLines = hasHeader ? 1 : 0;
            final CSVReader reader = new CSVReaderBuilder(br).withSkipLines(skipLines).build();
            rows = reader.readAll();
        }
        
        final int numRows = rows.size();
        final MetricData[] mpoints = new MetricData[numRows];
        final long incr = intervalInMinutes * 60L;
        
        long epochTimeInSeconds = startDate.getEpochSecond();
        for (int i = 0; i < numRows; i++) {
            final String[] row = rows.get(i);
            final Float value = Float.parseFloat(row[0]);
            mpoints[i] = toMetricData(metric, epochTimeInSeconds, value);
            epochTimeInSeconds += incr;
        }
        
        return new MetricFrame(mpoints);
    }
    
    private static MetricData toMetricData(MetricDefinition metric, String[] row) {
        final MetricData mpoint = new MetricData(metric,Float.parseFloat(row[1]),Instant.parse(row[0]).getEpochSecond());
        return mpoint;
    }
    
    private static MetricData toMetricData(MetricDefinition metric, long epochTimeInSeconds, Float value) {
        final MetricData mpoint = new MetricData(metric,value,epochTimeInSeconds);
        return mpoint;
    }
}
