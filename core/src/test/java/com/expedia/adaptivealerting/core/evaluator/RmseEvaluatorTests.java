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
package com.expedia.adaptivealerting.core.evaluator;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ListIterator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.opencsv.bean.CsvToBeanBuilder;

/**
 * @author kashah
 *
 */

public class RmseEvaluatorTests {

    private static List<RmseTestRow> calInflowTestRows;

    @BeforeClass
    public static void setUpClass() throws IOException {
        readData_calInflow();
    }

    // Class under test
    private RmseEvaluator evaluator;

    @Before
    public void setUp() {
        this.evaluator = new RmseEvaluator();
    }

    @Test
    public void testScore() {
        final ListIterator<RmseTestRow> testRows = calInflowTestRows.listIterator();
        while (testRows.hasNext()) {
            final RmseTestRow testRow = testRows.next();
            final double observed = testRow.getObserved();
            final double predicted = testRow.getPredicted();
            evaluator.update(observed, predicted);
            assertEquals(testRow.getRmse(), evaluator.evaluate(), 0);
        }
    }

    private static void readData_calInflow() {
        final InputStream is = ClassLoader.getSystemResourceAsStream("tests/cal-inflow-tests-rmse.csv");
        calInflowTestRows = new CsvToBeanBuilder<RmseTestRow>(new InputStreamReader(is)).withType(RmseTestRow.class)
                .build().parse();
    }

}
