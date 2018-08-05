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

import com.expedia.adaptivealerting.core.data.Metric;
import com.expedia.adaptivealerting.core.data.MetricFrame;
import com.expedia.adaptivealerting.core.data.Mpoint;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    public static MetricFrame loadCsv(Metric metric, File file, boolean hasHeader) throws IOException {
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
    public static MetricFrame loadCsv(Metric metric, InputStream in, boolean hasHeader) throws IOException {
        List<String[]> rows;
        try (final BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            final int skipLines = hasHeader ? 1 : 0;
            final CSVReader reader = new CSVReaderBuilder(br).withSkipLines(skipLines).build();
            rows = reader.readAll();
        }
        
        final int numRows = rows.size();
        final Mpoint[] mpoints = new Mpoint[numRows];
        
        for (int i = 0; i < numRows; i++) {
            mpoints[i] = toMpoint(metric, rows.get(i));
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
            Metric metric,
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
        final Mpoint[] mpoints = new Mpoint[numRows];
        final long incr = intervalInMinutes * 60L;
        
        long epochTimeInSeconds = startDate.getEpochSecond();
        for (int i = 0; i < numRows; i++) {
            final String[] row = rows.get(i);
            final Float value = Float.parseFloat(row[0]);
            mpoints[i] = toMpoint(metric, epochTimeInSeconds, value);
            epochTimeInSeconds += incr;
        }
        
        return new MetricFrame(mpoints);
    }
    
    private static Mpoint toMpoint(Metric metric, String[] row) {
        final Mpoint mpoint = new Mpoint();
        mpoint.setMetric(metric);
        mpoint.setEpochTimeInSeconds(Instant.parse(row[0]).getEpochSecond());
        mpoint.setValue(Float.parseFloat(row[1]));
        return mpoint;
    }
    
    private static Mpoint toMpoint(Metric metric, long epochTimeInSeconds, Float value) {
        final Mpoint mpoint = new Mpoint();
        mpoint.setMetric(metric);
        mpoint.setEpochTimeInSeconds(epochTimeInSeconds);
        mpoint.setValue(value);
        return mpoint;
    }
}
