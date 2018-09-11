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
package com.expedia.adaptivealerting.anomdetect.control;

import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.adaptivealerting.core.util.MathUtil;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.opencsv.bean.CsvToBeanBuilder;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;

/**
 * @author shsethi
 */
public class IndividualsChartDetectorTest {
    private static final int WARMUP_PERIOD = 25;
    private static final double TOLERANCE = 0.1;
    
    private MetricDefinition metricDefinition;
    private long epochSecond;
    private UUID uuid;
    private static List<IndividualsChartTestRow> data;
    
    
    @BeforeClass
    public static void setUpClass() {
        readDataFromCsv();
    }
    
    @Before
    public void setUp() {
        this.metricDefinition = new MetricDefinition("some-key");
        this.epochSecond = Instant.now().getEpochSecond();
        this.uuid = UUID.randomUUID();
    }
    
    @Test
    public void testEvaluate() {
        
        final ListIterator<IndividualsChartTestRow> testRows = data.listIterator();
        final IndividualsChartTestRow testRow0 = testRows.next();
        final double observed0 = testRow0.getObserved();
        final IndividualsControlChartDetector detector = new IndividualsControlChartDetector(observed0, WARMUP_PERIOD);
        int noOfDataPoints = 1;
        
        // Params
        assertEquals(WARMUP_PERIOD, detector.getWarmUpPeriod());
        
        while (testRows.hasNext()) {
            final IndividualsChartTestRow testRow = testRows.next();
            final double observed = testRow.getObserved();
            
            final MappedMetricData mappedMetricData = toMappedMetricData(epochSecond, observed);
            AnomalyResult result = detector.classify(mappedMetricData).getAnomalyResult();
            
            if (noOfDataPoints < WARMUP_PERIOD) {
                assertEquals(AnomalyLevel.valueOf("UNKNOWN"), result.getAnomalyLevel());
            } else {
                assertApproxEqual(testRow.getUpperControlLimit_R(), detector.getUpperControlLimit_R());
                assertApproxEqual(testRow.getLowerControlLimit_X(), detector.getLowerControlLimit_X());
                assertApproxEqual(testRow.getUpperControlLimit_X(), detector.getUpperControlLimit_X());
                assertEquals(AnomalyLevel.valueOf(testRow.getAnomalyLevel()), result.getAnomalyLevel());
            }
            noOfDataPoints += 1;
        }
    }
    
    private static void assertApproxEqual(double d1, double d2) {
        TestCase.assertTrue(MathUtil.isApproximatelyEqual(d1, d2, TOLERANCE));
    }
    
    private static void readDataFromCsv() {
        final InputStream is = ClassLoader.getSystemResourceAsStream("tests/individual-chart-sample-input.csv");
        data = new CsvToBeanBuilder<IndividualsChartTestRow>(new InputStreamReader(is))
                .withType(IndividualsChartTestRow.class)
                .build()
                .parse();
    }
    
    private MappedMetricData toMappedMetricData(long epochSecond, double value) {
        final MetricData metricData = new MetricData(metricDefinition, value, epochSecond);
        return new MappedMetricData(metricData, uuid, "individuals-detector");
    }
}
