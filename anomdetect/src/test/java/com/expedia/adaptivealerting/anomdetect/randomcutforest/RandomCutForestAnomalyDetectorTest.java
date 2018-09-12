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
package com.expedia.adaptivealerting.anomdetect.randomcutforest;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBeanBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import static junit.framework.TestCase.assertNotNull;

/**
 * @author Tatjana Kamenov
 */
public class RandomCutForestAnomalyDetectorTest {
    private UUID detectorUUID;
    
    @Before
    public void setUp() {
        this.detectorUUID = UUID.randomUUID();
    }

    @Test
    public void testDefaultConstructor() {
        final RandomCutForestAnomalyDetector detector = new RandomCutForestAnomalyDetector(detectorUUID);
        assertNotNull(detector);
    }

    @Test
    public void evaluate() {
        final ListIterator<RandomCutForestTestRow> testRows = readDataStream().listIterator();
        final RandomCutForestTestRow testRow0 = testRows.next();
        final RandomCutForestAnomalyDetector detector = new RandomCutForestAnomalyDetector(detectorUUID);

        while (testRows.hasNext()) {
            final RandomCutForestTestRow testRow = testRows.next();
            final double observed = testRow.getObserved();
            // TODO
        }
    }

    private static List<String[]> readCsv(String path) throws IOException {
        final InputStream is = ClassLoader.getSystemResourceAsStream(path);
        CSVReader reader = new CSVReader(new InputStreamReader(is));
        return reader.readAll();
    }

    private static List<RandomCutForestTestRow> readDataStream() {
        final InputStream is = ClassLoader.getSystemResourceAsStream("datasets/rcf-inflow.txt");
        return new CsvToBeanBuilder<RandomCutForestTestRow>(new InputStreamReader(is))
                .withType(RandomCutForestTestRow.class)
                .build()
                .parse();
    }
}
