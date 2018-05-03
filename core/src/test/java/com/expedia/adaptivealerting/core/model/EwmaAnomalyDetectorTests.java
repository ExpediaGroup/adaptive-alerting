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
package com.expedia.adaptivealerting.core.model;

import com.expedia.adaptivealerting.core.AnomalyLevel;
import com.opencsv.bean.CsvToBeanBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ListIterator;

import static com.expedia.adaptivealerting.core.util.MathUtil.isApproximatelyEqual;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * @author Willie Wheeler
 */
public class EwmaAnomalyDetectorTests {
    private static final double TOLERANCE = 0.01;
    
    private static List<EwmaTestRow> calInflowTestRows;
    
    @BeforeClass
    public static void setUpClass() throws IOException {
        readData_calInflow();
    }
    
    @Test
    public void testEvaluate() {
        
        // Params
        final double alpha = 0.05;
        final double smallMult = 2.0;
        final double largeMult = 3.0;
        
        final ListIterator<EwmaTestRow> testRows = calInflowTestRows.listIterator();
        final EwmaTestRow testRow0 = testRows.next();
        final double observed0 = testRow0.getObserved();
        final EwmaAnomalyDetector detector = new EwmaAnomalyDetector(alpha, smallMult, largeMult, observed0);
    
        // Params
        assertEquals(alpha, detector.getAlpha());
        assertEquals(smallMult, detector.getSmallMultiplier());
        assertEquals(largeMult, detector.getLargeMultiplier());
        
        // Seed observation
        assertEquals(observed0, detector.getMean());
        assertEquals(0.0, detector.getVariance());
        
        while (testRows.hasNext()) {
            final EwmaTestRow testRow = testRows.next();
            final int observed = testRow.getObserved();
            final String expectedLevel = testRow.getLevel();
            final AnomalyLevel actualLevel = detector.evaluate(observed);
            
            assertApproxEqual(testRow.getMean(), detector.getMean());
            assertApproxEqual(testRow.getVar(), detector.getVariance());
    
            if (!expectedLevel.isEmpty()) {
                assertEquals(expectedLevel, actualLevel.name());
            }
        }
    }
    
    private static void readData_calInflow() {
        final InputStream is = ClassLoader.getSystemResourceAsStream("datasets/cal-inflow-tests-ewma.csv");
        calInflowTestRows = new CsvToBeanBuilder<EwmaTestRow>(new InputStreamReader(is))
                .withType(EwmaTestRow.class)
                .build()
                .parse();

    }
    
    private static void assertApproxEqual(double d1, double d2) {
        assertTrue(isApproximatelyEqual(d1, d2, TOLERANCE));
    }
}
