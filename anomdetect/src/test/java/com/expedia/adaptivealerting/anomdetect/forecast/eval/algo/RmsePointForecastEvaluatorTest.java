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
package com.expedia.adaptivealerting.anomdetect.forecast.eval.algo;

import com.opencsv.bean.CsvToBeanBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RmsePointForecastEvaluatorTest {

    private static List<RmseEvaluatorTestRow> calInflowTestRows;

    @BeforeClass
    public static void setUpClass() {
        readData_calInflow();
    }

    // Class under test
    private RmsePointForecastEvaluator evaluator;

    @Before
    public void setUp() {
        this.evaluator = new RmsePointForecastEvaluator();
    }

    @Test
    public void testScore() {
        for (RmseEvaluatorTestRow testRow : calInflowTestRows) {
            final double observed = testRow.getObserved();
            final double predicted = testRow.getPredicted();
            evaluator.update(observed, predicted);
            assertEquals(testRow.getRmse(), evaluator.evaluate().getEvaluatorScore(), 0);
        }
    }

    private static void readData_calInflow() {
        final InputStream is = ClassLoader.getSystemResourceAsStream("datasets/cal-inflow-tests-rmse.csv");
        calInflowTestRows = new CsvToBeanBuilder<RmseEvaluatorTestRow>(new InputStreamReader(is)).withType(RmseEvaluatorTestRow.class)
                .build().parse();
    }

}
