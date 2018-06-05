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
package com.expedia.adaptivealerting.tools.pipeline.source;

import com.expedia.www.haystack.commons.entities.MetricPoint;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import static com.expedia.adaptivealerting.core.util.MetricPointUtil.metricPoint;

/**
 * Metric source backed by a CSV source.
 *
 * @author Willie Wheeler
 */
public final class CsvMetricSource extends AbstractMetricSource {
    private ListIterator<Double> data;
    
    /**
     * Creates a metric source backed by the given CSV source. We assume a single numeric column with no header.
     *
     * @param csvIn  CSV input stream.
     * @param name   Metric name.
     * @param period Timer period in milliseconds.
     */
    public CsvMetricSource(InputStream csvIn, String name, long period) {
        super(name, period);
        
        try {
            this.data = readData(csvIn);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public MetricPoint next() {
        return data.hasNext() ? metricPoint(getName(), Instant.now(), data.next().floatValue()) : null;
    }
    
    private ListIterator<Double> readData(InputStream csvIn) throws IOException {
        try (final BufferedReader br = new BufferedReader(new InputStreamReader(csvIn, StandardCharsets.UTF_8))) {
            final CSVReaderBuilder builder = new CSVReaderBuilder(br);
            final CSVReader reader = builder.build();
            final List<String[]> rows = reader.readAll();
            
            return rows.stream()
                    .map(row -> Double.parseDouble(row[0]))
                    .collect(Collectors.toList())
                    .listIterator();
        }
    }
    
}
